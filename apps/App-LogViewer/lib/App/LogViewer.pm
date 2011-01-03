use 5.010;
package App::LogViewer;
# ABSTRACT: Gtk2 IRC log viewer for PC which is related to LogViewer::Web

use Moose;
use autodie;
use namespace::autoclean;
use Readonly;
use File::HomeDir;
use File::Spec::Functions;
use JSON;
use DateTime;
use LWP::Simple;
use App::LogViewer::Config;
use App::LogViewer::Cache;
use App::LogViewer::Widget;

Readonly::Scalar my $CONF_DIR  => '.logviewer';
Readonly::Scalar my $CONF_FILE => 'config.ini';

has config_file => (
    is      => 'ro',
    isa     => 'Str',
    default => catfile( File::HomeDir->my_home, $CONF_DIR, $CONF_FILE ),
);

has channel => (
    is         => 'rw',
    isa        => 'Str',
    default    => q{},
);

has date => (
    is         => 'rw',
    isa        => 'DateTime',
    default    => sub { DateTime->now },
);

has _config => (
    is         => 'ro',
    isa        => 'App::LogViewer::Config',
    lazy_build => 1,
);

has _widget => (
    is         => 'ro',
    isa        => 'App::LogViewer::Widget',
    default    => sub { App::LogViewer::Widget->new; },
);

has _cache => (
    is         => 'ro',
    isa        => 'App::LogViewer::Cache',
    lazy_build => 1,
);

sub BUILD {
    my $self = shift;
    $self->_update_talk;
    return $self;
}

after channel => sub { $_[0]->_update_talk if $_[1] };
after date    => sub { $_[0]->_update_talk if $_[1] };

sub _update_talk {
    my $self = shift;

    return unless $self->_config->connect_host;
    return unless $self->channel;
    return unless $self->date;

    my $url     = $self->_config->connect_host;
    my $channel = $self->channel;
    my $year    = $self->date->year;
    my $month   = $self->date->month;
    my $day     = $self->date->day;

    #
    # Check cache
    #
    my $key = "/$channel/$year/$month/$day";
    my $content = $self->_cache->load($key);
    if (!defined $content) {
        my $content = get($url . $key);
        return unless defined $content;
        $self->_cache->save($key, $content);
    }

    my $messages = decode_json($content)->{data};

    my @formatted = map {
        my $dt = DateTime->from_epoch(
            epoch     => $_->[1],
            time_zone => 'Asia/Seoul',
        );

        my $timestamp = $dt->hms;
        my $nick = $_->[0];
        $nick =~ s/_+$//;

        my $talk = $_->[2];
        $talk =~ s/&/&amp;/g;
        $talk =~ s/</&lt;/g;
        $talk =~ s/>/&gt;/g;
        #$talk =~ s{(https?:\S*?)}{<a href="$1">$1</a>}g;
        #$talk .= "Go to the <a href=\"http://www.gtk.org\" title=\"&lt;i&gt;Our&lt;/i&gt; website\">GTK+ website</a> for more...";

        [
            $dt->hms,
            qq{<span weight="bold">$nick</span>},
            $talk,
        ];
    } @$messages;

    @{ $self->_widget->treeview->{data} } = @formatted;
}

sub _build__config {
    my $self = shift;
    my $config = App::LogViewer::Config->new(
        config_file => $self->config_file,
    );
    return $config;
}

sub _build__cache {
    my $self = shift;
    my $cache = App::LogViewer::Cache->new(
        cache_dir => $self->_config->cache_dir,
    );
    return $cache;
}

sub get_talk_vbox {
    my $self = shift;
    return $self->_widget->vbox;
}

sub get_talk_treeview {
    my $self = shift;
    return $self->_widget->treeview;
}

__PACKAGE__->meta->make_immutable;
no Moose;
1;
__END__

=head1 SYNOPSIS

    use App::LogViewer;
    
    my $lv = App::LogViewer->new;
    say $lv->config->connect_host;


=attr config_file

Specify config file to read.
Default config file is C<~/.logviewer/config.ini>.

    my $lv = App::LogViewer->new(
        config_file => 'my-config.ini',
    );


=method get_talk_widget
