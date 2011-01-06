package irclog;
use Dancer ':syntax';
use Dancer::Plugin::Database;
use DateTime;
use Data::Dumper;

our $VERSION = '0.1';

set serializer => 'JSON';

sub hhmm {
	my ($sec, $min, $hour) = localtime(shift);
	return sprintf '%02d:%02d', $hour, $min;
}

sub mm { my $time = shift; $time - $time % 60; }


sub make_result {
	my ($ary_ref) = @_;

	my %result;
	if($ary_ref) {
		%result = (result => 200, data => $ary_ref);
	}
	else {
		%result = (result => 500, message => database->errstr);
	}
	return \%result;
}

sub collapse {
	my ($ary_ref) = @_;
	my @collapse = ();
	foreach my $row (@$ary_ref) {
		if(@collapse == 0) {
			$collapse[0] = $row;
		}
		else {
			if($collapse[-1][0] eq $row->[0] and mm($collapse[-1][1]) == mm($row->[1])) {
				$collapse[-1][2] .= "\n" . $row->[2];
			}
			else {
				push @collapse, $row;
			}
		}
	}
	#print Dumper(\@collapse);
	return make_result(\@collapse);
}

get '/' => sub {
	my $ary_ref = database->selectall_arrayref(qq/select channel from irclog group by channel/);
	foreach my $row (@$ary_ref) {
		$row->[0] =~ s/^#//;
	}
	return make_result($ary_ref);

};

get '/:channel/index' => sub {
	my $channel = '#' . params->{channel};
	my $ary_ref = database->selectall_arrayref(qq/
		select date_format(from_unixtime(timestamp), '%Y-%m-%d') yyyymmdd
			from irclog
			where channel = ?
			group by yyyymmdd/,
		{}, $channel);
	return make_result($ary_ref);
};

get '/:channel' => sub {
	my $channel = '#' . params->{channel};
	my $ary_ref = database->selectall_arrayref(qq/
		select date_format(from_unixtime(timestamp), '%Y') yyyy
			from irclog
			where channel = ?
			group by yyyy/,
		{}, $channel);
	return make_result($ary_ref);
};

get '/:channel/:yyyy' => sub {
	my $channel = '#' . params->{channel};
	my $yyyy = sprintf '%04d', params->{yyyy};
	my $ary_ref = database->selectall_arrayref(
		qq/select date_format(from_unixtime(timestamp), '%m') mm
			from irclog
			where channel = ?
			  and date_format(from_unixtime(timestamp), '%Y') = ? group by mm/,
		{}, $channel, $yyyy);
	return make_result($ary_ref);
};

get '/:channel/:yyyy/:mm' => sub {
	my $channel = '#' . params->{channel};
	my $yyyymm = sprintf '%04d-%02d', params->{yyyy}, params->{mm};
	my $ary_ref = database->selectall_arrayref(
		qq/select date_format(from_unixtime(timestamp), '%d') dd 
			from irclog
			where channel = ?
			  and date_format(from_unixtime(timestamp), '%Y-%m') = ? group by dd/,
		{}, $channel, $yyyymm);
	return make_result($ary_ref);
};

get '/:channel/:yyyy/:mm/:dd' => sub {
	my $channel = '#' . params->{channel};
	my $yyyymmdd = sprintf '%04d-%02d-%02d', params->{yyyy}, params->{mm}, params->{dd};
	my $ary_ref = database->selectall_arrayref(
		qq/select nick, timestamp, line
			from irclog
			where channel = ?
			  and date_format(from_unixtime(timestamp), '%Y-%m-%d') = ? order by timestamp/,
		{}, $channel, $yyyymmdd);
	return collapse($ary_ref);
};

get '/:channel/:yyyy/:mm/:dd/:current' => sub {
	my $channel = '#' . params->{channel};
	my $current = params->{current};
	my $yyyymmdd = sprintf '%04d-%02d-%02d', params->{yyyy}, params->{mm}, params->{dd};
	my $ary_ref = database->selectall_arrayref(
		qq/select nick, timestamp, line
			from irclog
			where channel = ?
			  and timestamp > ?
			  and date_format(from_unixtime(timestamp), '%Y-%m-%d') = ? order by timestamp/,
		{}, $channel, $current, $yyyymmdd);
	return collapse($ary_ref);
};

true;
