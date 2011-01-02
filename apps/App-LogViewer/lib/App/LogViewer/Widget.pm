package App::LogViewer::Widget;
# ABSTRACT: Gtk2 widgets for App::LogViewer

use Moose;
use MooseX::Configuration;
use autodie;
use namespace::autoclean;
use JSON;
use DateTime;
use LWP::Simple;
use Glib qw(TRUE FALSE);
use Acme::Gtk2::Ex::Builder;

has url => (
    is         => 'rw',
    isa        => 'Str',
    default    => q{},
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

has _app => (
    is         => 'ro',
    isa        => 'Acme::Gtk2::Ex::Builder',
    lazy_build => 1,
);

sub BUILD {
    my $self = shift;
    $self->_update_talk;
    return $self;
}

after url     => sub { $_[0]->_update_talk if $_[1] };
after channel => sub { $_[0]->_update_talk if $_[1] };
after date    => sub { $_[0]->_update_talk if $_[1] };

sub _build__app {
    my $self = shift;

    my $app = build {
        widget VBox => contain {
            info id      => 'talk-vbox';
            info packing => TRUE, TRUE, 1, 'start';

            widget ScrolledWindow => contain {
                info packing => TRUE, TRUE, 1, 'start';
                set  policy  => 'never', 'always';

                widget SimpleList => contain {
                    info id              => 'talk-treeview';
                    set  headers_visible => FALSE;
                    set  rules_hint      => TRUE;
                }, (
                    timestamp => 'markup',
                    nick      => 'markup',
                    message   => 'markup',
                );
            };
        };
    };

    my @cells;
    my @columns = $app->find('talk-treeview')->get_columns;

    @cells = $columns[0]->get_cells;
    $cells[0]->set('yalign', 0);

    @cells = $columns[1]->get_cells;
    $cells[0]->set('xalign', 1);
    $cells[0]->set('yalign', 0);

    @cells = $columns[2]->get_cells;
    $cells[0]->set('wrap-mode', 'char');
    $cells[0]->set('wrap-width', 300);

    return $app;
}

sub talk { $_[0]->_app->find('talk-vbox') }

sub _update_talk {
    my $self = shift;

    return unless $self->url;
    return unless $self->channel;
    return unless $self->date;

    my $url     = $self->url;
    my $channel = $self->channel;
    my $year    = $self->date->year;
    my $month   = $self->date->month;
    my $day     = $self->date->day;

    my $content = get($url . "/$channel/$year/$month/$day");
    return unless defined $content;

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

    @{ $self->_app->find('talk-treeview')->{data} } = @formatted;
}

__PACKAGE__->meta->make_immutable;
no Moose;
1;
__END__

=head1 SYNOPSIS

    use App::LogViewer;
    
    my $lv     = App::LogViewer->new;
    my $widget = $lv->widget;


=head1 DESCRIPTION

This module is used for C<App::LogViewer>.
See C<App::LogViewer> and C<LogViewer::Web>.


=method talk

Get C<Gtk2::VBox> which contains chat logs.

