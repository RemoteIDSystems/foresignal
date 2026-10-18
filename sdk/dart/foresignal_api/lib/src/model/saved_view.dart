//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:foresignal_api/src/model/condition.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'saved_view.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SavedView {
  /// Returns a new [SavedView] instance.
  SavedView({

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

    required  this.id,

    required  this.version,

    required  this.createdAt,

    required  this.updatedAt,
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


  final SavedViewTimeFieldEnum? timeField;



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



  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'version',
    required: true,
    includeIfNull: false,
  )


  final int version;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'updatedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime updatedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SavedView &&
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
      other.fields == fields &&
      other.id == id &&
      other.version == version &&
      other.createdAt == createdAt &&
      other.updatedAt == updatedAt;

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
        fields.hashCode +
        id.hashCode +
        version.hashCode +
        createdAt.hashCode +
        updatedAt.hashCode;

  factory SavedView.fromJson(Map<String, dynamic> json) => _$SavedViewFromJson(json);

  Map<String, dynamic> toJson() => _$SavedViewToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SavedViewTimeFieldEnum {
@JsonValue(r'effective')
effective(r'effective'),
@JsonValue(r'observed')
observed(r'observed'),
@JsonValue(r'event')
event(r'event'),
@JsonValue(r'ingested')
ingested(r'ingested');

const SavedViewTimeFieldEnum(this.value);

final String value;

@override
String toString() => value;
}


