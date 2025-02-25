package com.myapp.iib.model;


import com.holonplatform.core.property.PropertySet;
import com.holonplatform.core.property.StringProperty;

public  interface DummyListing
{
  StringProperty A = StringProperty.create("A");
  StringProperty B = StringProperty.create("B");
  PropertySet<?> PROPERTIES = PropertySet.of(A, B);
}