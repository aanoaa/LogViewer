package App::LogViewer::Widget;
# ABSTRACT: Gtk2 widgets for App::LogViewer

use Moose;
use MooseX::Configuration;
use autodie;
use namespace::autoclean;
use Glib qw(TRUE FALSE);
use Acme::Gtk2::Ex::Builder;

has _app => (
    is         => 'ro',
    isa        => 'Acme::Gtk2::Ex::Builder',
    lazy_build => 1,
);

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

sub vbox     { $_[0]->_app->find('talk-vbox')     }
sub treeview { $_[0]->_app->find('talk-treeview') }

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

