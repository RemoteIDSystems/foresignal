//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'exposure_places_items_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExposurePlacesItemsInner {
  /// Returns a new [ExposurePlacesItemsInner] instance.
  ExposurePlacesItemsInner({

    required  this.placeId,

    required  this.name,
  });

  @JsonKey(
    
    name: r'placeId',
    required: true,
    includeIfNull: false,
  )


  final String placeId;



  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExposurePlacesItemsInner &&
      other.placeId == placeId &&
      other.name == name;

    @override
    int get hashCode =>
        placeId.hashCode +
        name.hashCode;

  factory ExposurePlacesItemsInner.fromJson(Map<String, dynamic> json) => _$ExposurePlacesItemsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ExposurePlacesItemsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

