#!/usr/bin/env perl
use strict;
use warnings;
use DateTime;
die "Usage: $0 logfile\n" unless @ARGV;
open my $fh, '<', $ARGV[0] or die "Couldn't open $ARGV[0]\n";
my %hash;
while (chomp(my $line = <$fh>)) {
    my ($hour, $minute, $id, $content) = $line =~ m{^(\d{2}):(\d{2}) (\S+)\s+(.*)$};
    $content =~ s/'/\'/g;
    my $dt = DateTime->new(
        year        => '2010',
        month       => '12',
        day         => '11',
        hour        => $hour, 
        minute      => $minute, 
        time_zone   => 'Asia/Seoul'
    );

    unless ($hash{$dt->epoch}) {
        $hash{$dt->epoch} = $dt->epoch;
    } else {
        $hash{$dt->epoch}++;
    }

    print "INSERT INTO log values( '$id', '\\#perl-kr', '$id', '$id', 'freenode', '$content', " . $hash{$dt->epoch} . " );\n";
}


