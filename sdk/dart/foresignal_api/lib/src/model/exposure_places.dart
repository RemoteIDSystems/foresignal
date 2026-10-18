//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/attribution.dart';
import 'package:foresignal_api/src/model/exposure_places_items_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'exposure_places.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExposurePlaces {
  /// Returns a new [ExposurePlaces] instance.
  ExposurePlaces({

    required  this.status,

     this.attribution,

     this.items,
  });

  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final ExposurePlacesStatusEnum status;



  @JsonKey(
    
    name: r'attribution',
    required: false,
    includeIfNull: false,
  )


  final List<Attribution>? attribution;



  @JsonKey(
    
    name: r'items',
    required: false,
    includeIfNull: false,
  )


  final List<ExposurePlacesItemsInner>? items;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExposurePlaces &&
      other.status == status &&
      other.attribution == attribution &&
      other.items == items;

    @override
    int get hashCode =>
        status.hashCode +
        attribution.hashCode +
        items.hashCode;

  factory ExposurePlaces.fromJson(Map<String, dynamic> json) => _$ExposurePlacesFromJson(json);

  Map<String, dynamic> toJson() => _$ExposurePlacesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ExposurePlacesStatusEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'unavailable')
unavailable(r'unavailable'),
@JsonValue(r'not-installed')
notInstalled(r'not-installed');

const ExposurePlacesStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


