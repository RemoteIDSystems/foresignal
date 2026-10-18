//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'export_spec.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExportSpec {
  /// Returns a new [ExportSpec] instance.
  ExportSpec({

     this.name,

     this.format,

     this.sources,

     this.kinds,

     this.bbox,

     this.placeId,

     this.from,

     this.to,

     this.timeField,

     this.mode,

     this.asOf,

     this.atSeq,

     this.fields,

     this.flatten = false,

     this.includePrivate = false,

     this.personalUse = false,
  });

  @JsonKey(
    
    name: r'name',
    required: false,
    includeIfNull: false,
  )


  final String? name;



      /// gpx requires mode=history
  @JsonKey(
    
    name: r'format',
    required: false,
    includeIfNull: false,
  )


  final ExportSpecFormatEnum? format;



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



      /// [minLon, minLat, maxLon, maxLat] (WGS84)
  @JsonKey(
    
    name: r'bbox',
    required: false,
    includeIfNull: false,
  )


  final List<num>? bbox;



  @JsonKey(
    
    name: r'placeId',
    required: false,
    includeIfNull: false,
  )


  final String? placeId;



  @JsonKey(
    
    name: r'from',
    required: false,
    includeIfNull: false,
  )


  final DateTime? from;



  @JsonKey(
    
    name: r'to',
    required: false,
    includeIfNull: false,
  )


  final DateTime? to;



      /// 014 semantics for from/to
  @JsonKey(
    
    name: r'timeField',
    required: false,
    includeIfNull: false,
  )


  final ExportSpecTimeFieldEnum? timeField;



  @JsonKey(
    
    name: r'mode',
    required: false,
    includeIfNull: false,
  )


  final ExportSpecModeEnum? mode;



  @JsonKey(
    
    name: r'asOf',
    required: false,
    includeIfNull: false,
  )


  final DateTime? asOf;



  @JsonKey(
    
    name: r'atSeq',
    required: false,
    includeIfNull: false,
  )


  final int? atSeq;



      /// Payload allowlist
  @JsonKey(
    
    name: r'fields',
    required: false,
    includeIfNull: false,
  )


  final List<String>? fields;



      /// Promote fieldSpecs keys to columns
  @JsonKey(
    defaultValue: false,
    name: r'flatten',
    required: false,
    includeIfNull: false,
  )


  final bool? flatten;



      /// Include private sources (039); admin only
  @JsonKey(
    defaultValue: false,
    name: r'includePrivate',
    required: false,
    includeIfNull: false,
  )


  final bool? includePrivate;



      /// Include FORBIDDEN and LOCAL_ONLY records; the sidecar then says personal use only — do not redistribute
  @JsonKey(
    defaultValue: false,
    name: r'personalUse',
    required: false,
    includeIfNull: false,
  )


  final bool? personalUse;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExportSpec &&
      other.name == name &&
      other.format == format &&
      other.sources == sources &&
      other.kinds == kinds &&
      other.bbox == bbox &&
      other.placeId == placeId &&
      other.from == from &&
      other.to == to &&
      other.timeField == timeField &&
      other.mode == mode &&
      other.asOf == asOf &&
      other.atSeq == atSeq &&
      other.fields == fields &&
      other.flatten == flatten &&
      other.includePrivate == includePrivate &&
      other.personalUse == personalUse;

    @override
    int get hashCode =>
        (name == null ? 0 : name.hashCode) +
        format.hashCode +
        sources.hashCode +
        kinds.hashCode +
        (bbox == null ? 0 : bbox.hashCode) +
        (placeId == null ? 0 : placeId.hashCode) +
        (from == null ? 0 : from.hashCode) +
        (to == null ? 0 : to.hashCode) +
        timeField.hashCode +
        mode.hashCode +
        (asOf == null ? 0 : asOf.hashCode) +
        (atSeq == null ? 0 : atSeq.hashCode) +
        fields.hashCode +
        flatten.hashCode +
        includePrivate.hashCode +
        personalUse.hashCode;

  factory ExportSpec.fromJson(Map<String, dynamic> json) => _$ExportSpecFromJson(json);

  Map<String, dynamic> toJson() => _$ExportSpecToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// gpx requires mode=history
enum ExportSpecFormatEnum {
@JsonValue(r'geoparquet')
geoparquet(r'geoparquet'),
@JsonValue(r'geopackage')
geopackage(r'geopackage'),
@JsonValue(r'flatgeobuf')
flatgeobuf(r'flatgeobuf'),
@JsonValue(r'gpx')
gpx(r'gpx');

const ExportSpecFormatEnum(this.value);

final String value;

@override
String toString() => value;
}


/// 014 semantics for from/to
enum ExportSpecTimeFieldEnum {
@JsonValue(r'effective')
effective(r'effective'),
@JsonValue(r'observed')
observed(r'observed'),
@JsonValue(r'event')
event(r'event'),
@JsonValue(r'ingested')
ingested(r'ingested');

const ExportSpecTimeFieldEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ExportSpecModeEnum {
@JsonValue(r'current')
current(r'current'),
@JsonValue(r'history')
history(r'history'),
@JsonValue(r'asOf')
asOf(r'asOf'),
@JsonValue(r'atSeq')
atSeq(r'atSeq');

const ExportSpecModeEnum(this.value);

final String value;

@override
String toString() => value;
}


