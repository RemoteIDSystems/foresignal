//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:foresignal_api/src/model/condition.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'saved_view_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SavedViewInput {
  /// Returns a new [SavedViewInput] instance.
  SavedViewInput({

    required  this.slug,

    required  this.name,

     this.sources,

     this.kinds,

     this.placeIds,

     this.bbox,

     this.area,

     this.conditions,

     this.timeField,

     this.includeExpired = false,

     this.fields,
  });

  @JsonKey(
    
    name: r'slug',
    required: true,
    includeIfNull: false,
  )


  final String slug;



  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'sources',
    required: false,
    includeIfNull: false,
  )


  final List<String>? sources;



  @JsonKey(
    
    name: r'kinds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? kinds;



  @JsonKey(
    
    name: r'placeIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? placeIds;



      /// [minLon, minLat, maxLon, maxLat] (WGS84)
  @JsonKey(
    
    name: r'bbox',
    required: false,
    includeIfNull: false,
  )


  final List<num>? bbox;



  @JsonKey(
    
    name: r'area',
    required: false,
    includeIfNull: false,
  )


  final Geometry? area;



      /// 017 grammar including in/notIn/exists/matches/startsWith and inWatchlist/notInWatchlist
  @JsonKey(
    
    name: r'conditions',
    required: false,
    includeIfNull: false,
  )


  final List<Condition>? conditions;



  @JsonKey(
    
    name: r'timeField',
    required: false,
    includeIfNull: false,
  )


  final SavedViewInputTimeFieldEnum? timeField;



  @JsonKey(
    defaultValue: false,
    name: r'includeExpired',
    required: false,
    includeIfNull: false,
  )


  final bool? includeExpired;



      /// Projection whitelist of top-level-rooted JSON paths
  @JsonKey(
    
    name: r'fields',
    required: false,
    includeIfNull: false,
  )


  final List<String>? fields;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SavedViewInput &&
      other.slug == slug &&
      other.name == name &&
      other.sources == sources &&
      other.kinds == kinds &&
      other.placeIds == placeIds &&
      other.bbox == bbox &&
      other.area == area &&
      other.conditions == conditions &&
      other.timeField == timeField &&
      other.includeExpired == includeExpired &&
      other.fields == fields;

    @override
    int get hashCode =>
        slug.hashCode +
        name.hashCode +
        sources.hashCode +
        kinds.hashCode +
        placeIds.hashCode +
        (bbox == null ? 0 : bbox.hashCode) +
        (area == null ? 0 : area.hashCode) +
        conditions.hashCode +
        timeField.hashCode +
        includeExpired.hashCode +
        fields.hashCode;

  factory SavedViewInput.fromJson(Map<String, dynamic> json) => _$SavedViewInputFromJson(json);

  Map<String, dynamic> toJson() => _$SavedViewInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SavedViewInputTimeFieldEnum {
@JsonValue(r'effective')
effective(r'effective'),
@JsonValue(r'observed')
observed(r'observed'),
@JsonValue(r'event')
event(r'event'),
@JsonValue(r'ingested')
ingested(r'ingested');

const SavedViewInputTimeFieldEnum(this.value);

final String value;

@override
String toString() => value;
}


