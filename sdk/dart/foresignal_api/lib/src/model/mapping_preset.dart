//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/ingest_policy.dart';
import 'package:foresignal_api/src/model/field_spec.dart';
import 'package:foresignal_api/src/model/record_mapping.dart';
import 'package:foresignal_api/src/model/param_spec.dart';
import 'package:foresignal_api/src/model/mapping_preset_setup.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'mapping_preset.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MappingPreset {
  /// Returns a new [MappingPreset] instance.
  MappingPreset({

    required  this.id,

    required  this.title,

     this.description,

    required  this.format,

     this.mapping,

    required  this.kinds,

     this.fieldSpecs,

    required  this.observedAtMode,

     this.ttl,

     this.ingestPolicy,

     this.significantFields,

     this.volatileFields,

     this.private,

     this.allowPathKey,

     this.allowsGet,

    required  this.params,

    required  this.setup,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



  @JsonKey(
    
    name: r'description',
    required: false,
    includeIfNull: false,
  )


  final String? description;



      /// Ingest format id used at `/v1/ingest/{sourceId}/{format}`
  @JsonKey(
    
    name: r'format',
    required: true,
    includeIfNull: false,
  )


  final String format;



      /// null when the preset delegates to a built-in format (e.g. `alertmanager`)
  @JsonKey(
    
    name: r'mapping',
    required: false,
    includeIfNull: false,
  )


  final RecordMapping? mapping;



  @JsonKey(
    
    name: r'kinds',
    required: true,
    includeIfNull: false,
  )


  final List<String> kinds;



  @JsonKey(
    
    name: r'fieldSpecs',
    required: false,
    includeIfNull: false,
  )


  final Map<String, FieldSpec>? fieldSpecs;



  @JsonKey(
    
    name: r'observedAtMode',
    required: true,
    includeIfNull: false,
  )


  final MappingPresetObservedAtModeEnum observedAtMode;



      /// Record TTL (ISO-8601 duration or shorthand)
  @JsonKey(
    
    name: r'ttl',
    required: false,
    includeIfNull: false,
  )


  final String? ttl;



  @JsonKey(
    
    name: r'ingestPolicy',
    required: false,
    includeIfNull: false,
  )


  final IngestPolicy? ingestPolicy;



  @JsonKey(
    
    name: r'significantFields',
    required: false,
    includeIfNull: false,
  )


  final List<String>? significantFields;



  @JsonKey(
    
    name: r'volatileFields',
    required: false,
    includeIfNull: false,
  )


  final List<String>? volatileFields;



  @JsonKey(
    
    name: r'private',
    required: false,
    includeIfNull: false,
  )


  final bool? private;



  @JsonKey(
    
    name: r'allowPathKey',
    required: false,
    includeIfNull: false,
  )


  final bool? allowPathKey;



      /// The format accepts GET (wu-pws, gmc)
  @JsonKey(
    
    name: r'allowsGet',
    required: false,
    includeIfNull: false,
  )


  final bool? allowsGet;



      /// Preset parameters (e.g. lon, lat, exactLocation)
  @JsonKey(
    
    name: r'params',
    required: true,
    includeIfNull: false,
  )


  final List<ParamSpec> params;



  @JsonKey(
    
    name: r'setup',
    required: true,
    includeIfNull: false,
  )


  final MappingPresetSetup setup;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MappingPreset &&
      other.id == id &&
      other.title == title &&
      other.description == description &&
      other.format == format &&
      other.mapping == mapping &&
      other.kinds == kinds &&
      other.fieldSpecs == fieldSpecs &&
      other.observedAtMode == observedAtMode &&
      other.ttl == ttl &&
      other.ingestPolicy == ingestPolicy &&
      other.significantFields == significantFields &&
      other.volatileFields == volatileFields &&
      other.private == private &&
      other.allowPathKey == allowPathKey &&
      other.allowsGet == allowsGet &&
      other.params == params &&
      other.setup == setup;

    @override
    int get hashCode =>
        id.hashCode +
        title.hashCode +
        description.hashCode +
        format.hashCode +
        (mapping == null ? 0 : mapping.hashCode) +
        kinds.hashCode +
        fieldSpecs.hashCode +
        observedAtMode.hashCode +
        (ttl == null ? 0 : ttl.hashCode) +
        (ingestPolicy == null ? 0 : ingestPolicy.hashCode) +
        significantFields.hashCode +
        volatileFields.hashCode +
        private.hashCode +
        allowPathKey.hashCode +
        allowsGet.hashCode +
        params.hashCode +
        setup.hashCode;

  factory MappingPreset.fromJson(Map<String, dynamic> json) => _$MappingPresetFromJson(json);

  Map<String, dynamic> toJson() => _$MappingPresetToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum MappingPresetObservedAtModeEnum {
@JsonValue(r'UPSTREAM')
UPSTREAM(r'UPSTREAM'),
@JsonValue(r'RECEIVED')
RECEIVED(r'RECEIVED');

const MappingPresetObservedAtModeEnum(this.value);

final String value;

@override
String toString() => value;
}


