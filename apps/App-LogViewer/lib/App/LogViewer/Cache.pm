package App::LogViewer::Cache;
# ABSTRACT: Cache for App::LogViewer

use Moose;
use autodie;
use namespace::autoclean;
use CHI;

has cache_dir => (
    is      => 'ro',
    isa     => 'Str',
    default => q{cache},
);

has _cache => (
    is         => 'ro',
    isa        => 'Object',
    lazy_build => 1,
);

sub _build__cache {
    my $self = shift;

    my $cache = CHI->new(
        driver    => 'File',
        root_dir  => $self->cache_dir,
    );

    return $cache;
}

sub load {
    my $self = shift;
    my $name = shift;

    return $self->_cache->get($name);
}

sub save {
    my $self = shift;
    my $name = shift;
    my $data = shift;

    $self->_cache->set($name, $data);
}


__PACKAGE__->meta->make_immutable;
no Moose;
1;
__END__

=head1 SYNOPSIS

    use App::LogViewer::Cache;
    
    my $cache = App::LogViewer::Cache->new(
        cache_dir => '/home/keedi/.logviewer/cache',
    );
    
    my $value = $cache->load($key);
    if (!defined $value) {
        $value = ...
        $cache->save($key, $value);
    }


=head1 DESCRIPTION

This module is used for C<App::LogViewer>.
See C<App::LogViewer> and C<LogViewer::Web>.


=attr cache_dir

Cache directory.


=method load

Get data from the cache


=method save

Set data to the cache
