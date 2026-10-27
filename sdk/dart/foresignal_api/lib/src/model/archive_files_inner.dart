//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'archive_files_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ArchiveFilesInner {
  /// Returns a new [ArchiveFilesInner] instance.
  ArchiveFilesInner({

     this.name,

     this.sourceId,

     this.tier,

     this.fromSeq,

     this.toSeq,

     this.fromTime,

     this.toTime,

     this.rows,

     this.bytes,

     this.sha256,

     this.location,
  });

  @JsonKey(
    
    name: r'name',
    required: false,
    includeIfNull: false,
  )


  final String? name;



  @JsonKey(
    
    name: r'sourceId',
    required: false,
    includeIfNull: false,
  )


  final String? sourceId;



  @JsonKey(
    
    name: r'tier',
    required: false,
    includeIfNull: false,
  )


  final ArchiveFilesInnerTierEnum? tier;



  @JsonKey(
    
    name: r'fromSeq',
    required: false,
    includeIfNull: false,
  )


  final int? fromSeq;



  @JsonKey(
    
    name: r'toSeq',
    required: false,
    includeIfNull: false,
  )


  final int? toSeq;



  @JsonKey(
    
    name: r'fromTime',
    required: false,
    includeIfNull: false,
  )


  final DateTime? fromTime;



  @JsonKey(
    
    name: r'toTime',
    required: false,
    includeIfNull: false,
  )


  final DateTime? toTime;



  @JsonKey(
    
    name: r'rows',
    required: false,
    includeIfNull: false,
  )


  final int? rows;



  @JsonKey(
    
    name: r'bytes',
    required: false,
    includeIfNull: false,
  )


  final int? bytes;



  @JsonKey(
    
    name: r'sha256',
    required: false,
    includeIfNull: false,
  )


  final String? sha256;



  @JsonKey(
    
    name: r'location',
    required: false,
    includeIfNull: false,
  )


  final String? location;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ArchiveFilesInner &&
      other.name == name &&
      other.sourceId == sourceId &&
      other.tier == tier &&
      other.fromSeq == fromSeq &&
      other.toSeq == toSeq &&
      other.fromTime == fromTime &&
      other.toTime == toTime &&
      other.rows == rows &&
      other.bytes == bytes &&
      other.sha256 == sha256 &&
      other.location == location;

    @override
    int get hashCode =>
        name.hashCode +
        sourceId.hashCode +
        tier.hashCode +
        fromSeq.hashCode +
        toSeq.hashCode +
        (fromTime == null ? 0 : fromTime.hashCode) +
        (toTime == null ? 0 : toTime.hashCode) +
        rows.hashCode +
        bytes.hashCode +
        sha256.hashCode +
        (location == null ? 0 : location.hashCode);

  factory ArchiveFilesInner.fromJson(Map<String, dynamic> json) => _$ArchiveFilesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ArchiveFilesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ArchiveFilesInnerTierEnum {
@JsonValue(r'standard')
standard(r'standard'),
@JsonValue(r'track')
track(r'track');

const ArchiveFilesInnerTierEnum(this.value);

final String value;

@override
String toString() => value;
}


