use 5.010;
package App::LogViewer;
# ABSTRACT: Gtk2 IRC log viewer for PC which is related to LogViewer::Web

use Moose;
use autodie;
use namespace::autoclean;
use Readonly;
use File::HomeDir;
use File::Spec::Functions;
use App::LogViewer::Config;
use App::LogViewer::Widget;

Readonly::Scalar my $CONF_DIR  => '.logviewer';
Readonly::Scalar my $CONF_FILE => 'config.ini';

has config_file => (
    is      => 'ro',
    isa     => 'Str',
    default => catfile( File::HomeDir->my_home, $CONF_DIR, $CONF_FILE ),
);

has config => (
    is         => 'ro',
    isa        => 'App::LogViewer::Config',
    lazy_build => 1,
);

sub _build_config {
    my $self = shift;
    my $config = App::LogViewer::Config->new(
        config_file => $self->config_file,
    );
    return $config;
}

has widget => (
    is         => 'ro',
    isa        => 'App::LogViewer::Widget',
    lazy_build => 1,
);

sub _build_widget {
    my $self = shift;
    my $widget = App::LogViewer::Widget->new(
        url => $self->config->connect_host,
    );
    return $widget;
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


=method config

Get L<App::LogViewer::Config> object.

    my $lv = App::LogViewer->new;
    my $config = $lv->config;
    say $lv->config->connect_host;
