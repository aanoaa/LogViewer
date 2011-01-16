//
//  DetailViewController.m
//  IRCLogViewer
//
//  Created by Yuni Kunho Kim on 12/29/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "DetailViewController.h"

#define RKL_BLOCKS 0
// http://www.cocoabuilder.com/archive/cocoa/288966-applications-using-regexkitlite-no-longer-being-accepted-at-the-appstore.html
#import "RegexKitLite/RegexKitLite.h"


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

#define REGEX_URL @"([hH][tT][tT][pP][sS]?:\\/\\/[^ ,'\">\\]\\)]*[^\\. ,'\">\\]\\)])"
#define HTML_DETAIL @"<html><head><meta name=\"viewport\" content=\"width=320px;\"/><style>* { word-wrap: break-word; } a,a:link,a:visited { text-decoration: none; font-weight: 900; } body {margin: 0px; padding: 0px} .ct { margin: 5px; padding: 5px; width: 300px; height: 200px; background-color: #BBDDFF; }</style></head><body><div class=\"ct\">%@</div></body></html>"
- (NSString *)makeHTML {
	
	NSString *html = [[[NSString alloc] initWithFormat:HTML_DETAIL, [talk stringByReplacingOccurrencesOfRegex:REGEX_URL withString:@"<a href=\"$1\">$1</a>"]] autorelease];

	//NSLog(@"%@ => %@", talk, );
	//NSLog(@"%@", html);
	
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
