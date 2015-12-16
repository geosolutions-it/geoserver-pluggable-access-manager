<?xml version="1.0" encoding="ISO-8859-1"?>
   <StyledLayerDescriptor version="1.0.0"
       xsi:schemaLocation="http://www.opengis.net/sld StyledLayerDescriptor.xsd"
       xmlns="http://www.opengis.net/sld"
       xmlns:ogc="http://www.opengis.net/ogc"
       xmlns:xlink="http://www.w3.org/1999/xlink"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
     <NamedLayer>
       <Name>Wind speed Dynamic</Name>
       <UserStyle>
         <Title>Wind speed m/s</Title>
         <Abstract>Wind speed m/s</Abstract>
         <FeatureTypeStyle>
           <Transformation>
             <ogc:Function name="gs:DynamicColorMap">
               <ogc:Function name="parameter">
                 <ogc:Literal>data</ogc:Literal>
               </ogc:Function>
               <ogc:Function name="parameter">
                 <ogc:Literal>colorRamp</ogc:Literal>
                 <ogc:Function name="colormap">
                   <ogc:Literal>grass\bcyr</ogc:Literal>
                   <ogc:Function name="gridCoverageStats"><ogc:Literal>minimum</ogc:Literal></ogc:Function>
                   <ogc:Function name="gridCoverageStats"><ogc:Literal>maximum</ogc:Literal></ogc:Function>
                 </ogc:Function>
               </ogc:Function>
             </ogc:Function>
           </Transformation>
           <Rule>
            <Name>rule1</Name>
            <RasterSymbolizer>
              <Opacity>1.0</Opacity>
            </RasterSymbolizer>
           </Rule>
         </FeatureTypeStyle>
       </UserStyle>
     </NamedLayer>
    </StyledLayerDescriptor>