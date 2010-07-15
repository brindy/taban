/*
 * AppController.j
 * NewApplication
 *
 * Created by You on April 9, 2010.
 * Copyright 2010, Your Company All rights reserved.
 */

@import <Foundation/CPObject.j>
@import "TBBreadCrumbs.j"


@implementation AppController : CPObject
{
	
	TBBreadCrumbs crumbs;
	
	CPTextField aliasResponse;
	
	CPString theData;
	
	CPView contentView;
	
	
}

- (void)applicationDidFinishLaunching:(CPNotification)aNotification
{
	theData = @"";

    var theWindow = [[CPWindow alloc] initWithContentRect:CGRectMakeZero() styleMask:CPBorderlessBridgeWindowMask],
    	contentView = [theWindow contentView];

    var label = [[CPTextField alloc] initWithFrame:CGRectMakeZero()];

    [label setStringValue:@"Taban Browser"];
    [label setFont:[CPFont boldSystemFontOfSize:24.0]];
    [label sizeToFit];
    
    var center = [contentView center];
    center.y = 24;
    
	[label setCenter:center];
    [label setAutoresizingMask:CPViewMinXMargin | CPViewMaxXMargin | CPViewMaxYMargin];
    [contentView addSubview:label];

	crumbs = [[TBBreadCrumbs alloc] init];
    [crumbs setAutoresizingMask: CPViewMaxXMargin | CPViewMaxYMargin];

	var offsetY = CGRectGetMaxY([crumbs frame]) + 30;

	var split = [[CPSplitView alloc] initWithFrame:CGRectMake(
			10, offsetY, 
			CGRectGetWidth([contentView bounds]) - 20, 
			CGRectGetHeight([contentView bounds]) - offsetY - 10)];
    [contentView addSubview:split];
	
	var buckets = [[CPView alloc] initWithFrame:CGRectMakeZero()];
	[buckets setBackgroundColor:[CPColor redColor]];
	[buckets setAutoresizingMask:CPViewHeightSizable | CPViewMaxXMargin];    
	[split addSubview:buckets];

	var content = [[CPView alloc] initWithFrame:CGRectMakeZero()];
	[content setBackgroundColor:[CPColor blueColor]];
	[content setAutoresizingMask:CPViewHeightSizable | CPViewMaxXMargin];    
	[split addSubview:content];
	
	[split setPosition:250 ofDividerAtIndex:0];
	[split ]
	
	var request = [CPURLRequest requestWithURL:@"alias"];
	[[CPURLConnection connectionWithRequest:request delegate:self] start];

    [theWindow orderFront:self];    
	    
}

-(void)connection:(URLConnection) aConnection didReceiveData:(CPString) data {
	
	theData = [theData stringByAppendingString:data];
	
	[crumbs push:[theData objectFromJSON].alias];
	
}

-(void)connectionDidFinishLoading:(CPURLConnection)connection {

 	var alias = [theData objectFromJSON];
		
	[contentView addSubview:crumbs];
	
}

@end
