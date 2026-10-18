//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'place_pack_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PlacePackRequest {
  /// Returns a new [PlacePackRequest] instance.
  PlacePackRequest({

     this.products,

     this.frames = 12,

     this.radiusKm = 150,

     this.maxZoom = 8,
  });

      /// Default (absent or empty): radar, IR and the place's forecast products. Products of sources the caller may not read are left out.
  @JsonKey(
    
    name: r'products',
    required: false,
    includeIfNull: false,
  )


  final List<String>? products;



          // minimum: 1
          // maximum: 12
  @JsonKey(
    defaultValue: 12,
    name: r'frames',
    required: false,
    includeIfNull: false,
  )


  final int? frames;



          // minimum: 0
  @JsonKey(
    defaultValue: 150,
    name: r'radiusKm',
    required: false,
    includeIfNull: false,
  )


  final num? radiusKm;



          // minimum: 0
          // maximum: 8
  @JsonKey(
    defaultValue: 8,
    name: r'maxZoom',
    required: false,
    includeIfNull: false,
  )


  final int? maxZoom;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PlacePackRequest &&
      other.products == products &&
      other.frames == frames &&
      other.radiusKm == radiusKm &&
      other.maxZoom == maxZoom;

    @override
    int get hashCode =>
        products.hashCode +
        frames.hashCode +
        radiusKm.hashCode +
        maxZoom.hashCode;

  factory PlacePackRequest.fromJson(Map<String, dynamic> json) => _$PlacePackRequestFromJson(json);

  Map<String, dynamic> toJson() => _$PlacePackRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

