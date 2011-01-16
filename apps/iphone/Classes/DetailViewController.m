//
//  DetailViewController.m
//  IRCLogViewer
//
//  Created by Yuni Kunho Kim on 12/29/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "DetailViewController.h"

@implementation DetailViewController
@synthesize nickLabel, datetimeLabel, webView;
@synthesize nick, datetime, talk;

// The designated initializer.  Override if you create the controller programmatically and want to perform customization that is not appropriate for viewDidLoad.
/*
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization.
    }
    return self;
}
*/

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [super viewDidLoad];
	
	nickLabel.text = nick;
	datetimeLabel.text = datetime;
	[self.webView loadHTMLString:[self makeHTML] baseURL:[NSURL URLWithString:@""]];
}

- (NSString *)makeHTML {
	NSString *html = [[[NSString alloc]initWithFormat:@"<html><head></head><body>%@</body></html>", talk] autorelease];
	NSLog(@"%@", html);
	return html;
}

/*
// Override to allow orientations other than the default portrait orientation.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations.
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}
*/

- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc. that aren't in use.
}

- (void)viewDidUnload {
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}


- (void)dealloc {
	self.webView = nil;
	[nick release];
	[datetime release];
	[talk release];
    [super dealloc];
}


@end
