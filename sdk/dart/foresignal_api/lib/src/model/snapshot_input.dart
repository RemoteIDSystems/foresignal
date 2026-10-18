//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'snapshot_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SnapshotInput {
  /// Returns a new [SnapshotInput] instance.
  SnapshotInput({

     this.name,

     this.atSeq,

     this.asOf,

     this.sources,

     this.kinds,

     this.bbox,

     this.includePrivate = false,

     this.personalUse = false,

     this.expiresAt,
  });

  @JsonKey(
    
    name: r'name',
    required: false,
    includeIfNull: false,
  )


  final String? name;



  @JsonKey(
    
    name: r'atSeq',
    required: false,
    includeIfNull: false,
  )


  final int? atSeq;



  @JsonKey(
    
    name: r'asOf',
    required: false,
    includeIfNull: false,
  )


  final DateTime? asOf;



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



      /// Include private sources (039); the endpoint is admin-only
  @JsonKey(
    defaultValue: false,
    name: r'includePrivate',
    required: false,
    includeIfNull: false,
  )


  final bool? includePrivate;



      /// Include FORBIDDEN and LOCAL_ONLY sources; the sidecar then says personal use only — do not redistribute
  @JsonKey(
    defaultValue: false,
    name: r'personalUse',
    required: false,
    includeIfNull: false,
  )


  final bool? personalUse;



      /// Snapshots never expire unless set
  @JsonKey(
    
    name: r'expiresAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? expiresAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SnapshotInput &&
      other.name == name &&
      other.atSeq == atSeq &&
      other.asOf == asOf &&
      other.sources == sources &&
      other.kinds == kinds &&
      other.bbox == bbox &&
      other.includePrivate == includePrivate &&
      other.personalUse == personalUse &&
      other.expiresAt == expiresAt;

    @override
    int get hashCode =>
        (name == null ? 0 : name.hashCode) +
        (atSeq == null ? 0 : atSeq.hashCode) +
        (asOf == null ? 0 : asOf.hashCode) +
        sources.hashCode +
        kinds.hashCode +
        (bbox == null ? 0 : bbox.hashCode) +
        includePrivate.hashCode +
        personalUse.hashCode +
        (expiresAt == null ? 0 : expiresAt.hashCode);

  factory SnapshotInput.fromJson(Map<String, dynamic> json) => _$SnapshotInputFromJson(json);

  Map<String, dynamic> toJson() => _$SnapshotInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

