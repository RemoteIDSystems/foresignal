//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'usage_point.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class UsagePoint {
  /// Returns a new [UsagePoint] instance.
  UsagePoint({

    required  this.t,

    required  this.requests,

    required  this.bytes,

    required  this.errors,

    required  this.refused,

     this.recordsSeen,

     this.versions,

     this.liveUpdates,

     this.touches,

     this.late_,

     this.skipped,

     this.rejected,

     this.versionBytes,
  });

  @JsonKey(
    
    name: r't',
    required: true,
    includeIfNull: false,
  )


  final DateTime t;



  @JsonKey(
    
    name: r'requests',
    required: true,
    includeIfNull: false,
  )


  final int requests;



  @JsonKey(
    
    name: r'bytes',
    required: true,
    includeIfNull: false,
  )


  final int bytes;



  @JsonKey(
    
    name: r'errors',
    required: true,
    includeIfNull: false,
  )


  final int errors;



  @JsonKey(
    
    name: r'refused',
    required: true,
    includeIfNull: false,
  )


  final int refused;



          // minimum: 0
  @JsonKey(
    
    name: r'recordsSeen',
    required: false,
    includeIfNull: false,
  )


  final int? recordsSeen;



          // minimum: 0
  @JsonKey(
    
    name: r'versions',
    required: false,
    includeIfNull: false,
  )


  final int? versions;



          // minimum: 0
  @JsonKey(
    
    name: r'liveUpdates',
    required: false,
    includeIfNull: false,
  )


  final int? liveUpdates;



          // minimum: 0
  @JsonKey(
    
    name: r'touches',
    required: false,
    includeIfNull: false,
  )


  final int? touches;



          // minimum: 0
  @JsonKey(
    
    name: r'late',
    required: false,
    includeIfNull: false,
  )


  final int? late_;



          // minimum: 0
  @JsonKey(
    
    name: r'skipped',
    required: false,
    includeIfNull: false,
  )


  final int? skipped;



          // minimum: 0
  @JsonKey(
    
    name: r'rejected',
    required: false,
    includeIfNull: false,
  )


  final int? rejected;



          // minimum: 0
  @JsonKey(
    
    name: r'versionBytes',
    required: false,
    includeIfNull: false,
  )


  final int? versionBytes;





    @override
    bool operator ==(Object other) => identical(this, other) || other is UsagePoint &&
      other.t == t &&
      other.requests == requests &&
      other.bytes == bytes &&
      other.errors == errors &&
      other.refused == refused &&
      other.recordsSeen == recordsSeen &&
      other.versions == versions &&
      other.liveUpdates == liveUpdates &&
      other.touches == touches &&
      other.late_ == late_ &&
      other.skipped == skipped &&
      other.rejected == rejected &&
      other.versionBytes == versionBytes;

    @override
    int get hashCode =>
        t.hashCode +
        requests.hashCode +
        bytes.hashCode +
        errors.hashCode +
        refused.hashCode +
        recordsSeen.hashCode +
        versions.hashCode +
        liveUpdates.hashCode +
        touches.hashCode +
        late_.hashCode +
        skipped.hashCode +
        rejected.hashCode +
        versionBytes.hashCode;

  factory UsagePoint.fromJson(Map<String, dynamic> json) => _$UsagePointFromJson(json);

  Map<String, dynamic> toJson() => _$UsagePointToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

