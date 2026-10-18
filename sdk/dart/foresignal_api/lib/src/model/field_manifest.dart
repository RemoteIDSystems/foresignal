//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/attribution.dart';
import 'package:foresignal_api/src/model/field_encodings.dart';
import 'package:foresignal_api/src/model/field_manifest_grid.dart';
import 'package:foresignal_api/src/model/field_manifest_components_inner.dart';
import 'package:foresignal_api/src/model/field_manifest_cycle.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'field_manifest.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class FieldManifest {
  /// Returns a new [FieldManifest] instance.
  FieldManifest({

    required  this.schemaVersion,

    required  this.model,

    required  this.cycle,

    required  this.level,

    required  this.units,

    required  this.grid,

    required  this.stale,

    required  this.unavailable,

     this.reason,

    required  this.components,

    required  this.byteLength,

    required  this.sha256,

     this.attribution,

    required  this.dataUrl,

     this.encodings,
  });

  @JsonKey(
    
    name: r'schemaVersion',
    required: true,
    includeIfNull: false,
  )


  final FieldManifestSchemaVersionEnum schemaVersion;



  @JsonKey(
    
    name: r'model',
    required: true,
    includeIfNull: false,
  )


  final String model;



  @JsonKey(
    
    name: r'cycle',
    required: true,
    includeIfNull: false,
  )


  final FieldManifestCycle cycle;



  @JsonKey(
    
    name: r'level',
    required: true,
    includeIfNull: false,
  )


  final String level;



  @JsonKey(
    
    name: r'units',
    required: true,
    includeIfNull: false,
  )


  final FieldManifestUnitsEnum units;



  @JsonKey(
    
    name: r'grid',
    required: true,
    includeIfNull: false,
  )


  final FieldManifestGrid grid;



  @JsonKey(
    
    name: r'stale',
    required: true,
    includeIfNull: false,
  )


  final bool stale;



  @JsonKey(
    
    name: r'unavailable',
    required: true,
    includeIfNull: false,
  )


  final bool unavailable;



  @JsonKey(
    
    name: r'reason',
    required: false,
    includeIfNull: false,
  )


  final String? reason;



  @JsonKey(
    
    name: r'components',
    required: true,
    includeIfNull: false,
  )


  final List<FieldManifestComponentsInner> components;



      /// Raw byte length (1°: 521,280)
  @JsonKey(
    
    name: r'byteLength',
    required: true,
    includeIfNull: false,
  )


  final int byteLength;



  @JsonKey(
    
    name: r'sha256',
    required: true,
    includeIfNull: false,
  )


  final String sha256;



  @JsonKey(
    
    name: r'attribution',
    required: false,
    includeIfNull: false,
  )


  final Attribution? attribution;



      /// Content-addressed `.f32` URL (run pinned, bbox snapped)
  @JsonKey(
    
    name: r'dataUrl',
    required: true,
    includeIfNull: false,
  )


  final String dataUrl;



  @JsonKey(
    
    name: r'encodings',
    required: false,
    includeIfNull: false,
  )


  final FieldEncodings? encodings;





    @override
    bool operator ==(Object other) => identical(this, other) || other is FieldManifest &&
      other.schemaVersion == schemaVersion &&
      other.model == model &&
      other.cycle == cycle &&
      other.level == level &&
      other.units == units &&
      other.grid == grid &&
      other.stale == stale &&
      other.unavailable == unavailable &&
      other.reason == reason &&
      other.components == components &&
      other.byteLength == byteLength &&
      other.sha256 == sha256 &&
      other.attribution == attribution &&
      other.dataUrl == dataUrl &&
      other.encodings == encodings;

    @override
    int get hashCode =>
        schemaVersion.hashCode +
        model.hashCode +
        cycle.hashCode +
        level.hashCode +
        units.hashCode +
        grid.hashCode +
        stale.hashCode +
        unavailable.hashCode +
        (reason == null ? 0 : reason.hashCode) +
        components.hashCode +
        byteLength.hashCode +
        sha256.hashCode +
        attribution.hashCode +
        dataUrl.hashCode +
        encodings.hashCode;

  factory FieldManifest.fromJson(Map<String, dynamic> json) => _$FieldManifestFromJson(json);

  Map<String, dynamic> toJson() => _$FieldManifestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum FieldManifestSchemaVersionEnum {
@JsonValue(1)
number1('1');

const FieldManifestSchemaVersionEnum(this.value);

final String value;

@override
String toString() => value;
}


enum FieldManifestUnitsEnum {
@JsonValue(r'm/s')
mSlashS(r'm/s');

const FieldManifestUnitsEnum(this.value);

final String value;

@override
String toString() => value;
}


