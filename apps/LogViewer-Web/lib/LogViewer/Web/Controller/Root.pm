package LogViewer::Web::Controller::Root;
use Moose;
use JSON qw/to_json/;
use DateTime;
use namespace::autoclean;

BEGIN { extends 'Catalyst::Controller' }

#
# Sets the actions in this controller to be registered with no prefix
# so they function identically to actions created in MyApp.pm
#
__PACKAGE__->config(namespace => '');

=head1 NAME

LogViewer::Web::Controller::Root - Root Controller for LogViewer::Web

=head1 DESCRIPTION

[enter your description here]

=head1 METHODS

=head2 index

The root page (/)

=cut

sub index :Path :Args(0) {
    my ( $self, $c ) = @_;

    # Hello World
    $c->response->body( $c->welcome_message );
}

=head2 default

Standard 404 error page

=cut

sub default :Path {
    my ( $self, $c ) = @_;
    $c->response->body( 'Page not found' );
    $c->response->status(404);
}

=head2 log

from: YYYY-MM-DD 로 받고
epoch: unixtime 으로 받아서
from 일에 해당하는 epoch 이후의 로그에 대한 \@array 를 json 으로 응답한다.

=cut

sub log :Local {
    my ( $self, $c, $from, $epoch ) = @_;
    if (defined $from) {
        $c->stash->{from} = $from;
        my ($year, $month, $day) = $from =~ m/^(\d{4})-(\d{2})-(\d{2})$/;
        if (!defined $year or !defined $month or !defined $day) {
            $c->stash->{errstr} = 'invalid argument';
            $c->detach('View::JSON')
        }

        my $dt = DateTime->new(
            year => $year, 
            month => $month, 
            day => $day
        );

        my $cond =  {
            -and => [
                'me.created_on' => { '>', $epoch ? $epoch : $dt->epoch }, 
                'me.created_on' => { '<=', $dt->epoch + 86399 }, 
            ]
        };

        #my @logs = $c->model('LogDB::Log')->search({ created_on => { '>', $from } });
        my @logs = $c->model('LogDB::Log')->search($cond);
        for my $log (@logs) {
            my $row = {
                channel => $log->channel, 
                nickname => $log->nickname, 
                username => $log->username, 
                hostname => $log->hostname, 
                message => $log->message, 
                created_on => $log->created_on, 
            };
            push @{ $c->stash->{rows} }, $row;
        }
    }

    $c->res->body(to_json($c->stash));
    #$c->forward('View::JSON'); # unicode 이슈가 있어서 Catalyst::View::JSON 을 사용못하것다. 내가 어케 하는지 모름
}

=head2 end

Attempt to render a view, if needed.

=cut

sub end : ActionClass('RenderView') {}

=head1 AUTHOR

hshong,,,

=head1 LICENSE

This library is free software. You can redistribute it and/or modify
it under the same terms as Perl itself.

=cut

__PACKAGE__->meta->make_immutable;

1;
