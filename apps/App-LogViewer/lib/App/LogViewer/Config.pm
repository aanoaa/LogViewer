package App::LogViewer::Config;
# ABSTRACT: Load configuration for App::LogViewer

use Moose;
use MooseX::Configuration;
use autodie;
use namespace::autoclean;

has connect_host => (
    is            => 'ro',
    isa           => 'Str',
    default       => q{},
    section       => 'connect',
    key           => 'host',
    documentation => 'Host to connect',
);

__PACKAGE__->meta->make_immutable;
no Moose;
1;
__END__

=head1 SYNOPSIS

    use App::LogViewer::Config;
    
    my $config = App::LogViewer::Config->new(
        config_file => '/home/keedi/.logviewer/config.ini',
    )
    
    say $config->connect_host;


=head1 DESCRIPTION

This module is used for C<App::LogViewer>.
See C<App::LogViewer> and C<LogViewer::Web>.
