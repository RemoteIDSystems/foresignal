//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'retention_preview_db_cap.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RetentionPreviewDbCap {
  /// Returns a new [RetentionPreviewDbCap] instance.
  RetentionPreviewDbCap({

     this.capBytes,

     this.sizeBytes,

     this.exceeded,

     this.windowDays,

     this.overwriteOldest,
  });

  @JsonKey(
    
    name: r'capBytes',
    required: false,
    includeIfNull: false,
  )


  final int? capBytes;



  @JsonKey(
    
    name: r'sizeBytes',
    required: false,
    includeIfNull: false,
  )


  final int? sizeBytes;



  @JsonKey(
    
    name: r'exceeded',
    required: false,
    includeIfNull: false,
  )


  final bool? exceeded;



      /// Window the cap has stepped down to, or null
  @JsonKey(
    
    name: r'windowDays',
    required: false,
    includeIfNull: false,
  )


  final int? windowDays;



  @JsonKey(
    
    name: r'overwriteOldest',
    required: false,
    includeIfNull: false,
  )


  final bool? overwriteOldest;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RetentionPreviewDbCap &&
      other.capBytes == capBytes &&
      other.sizeBytes == sizeBytes &&
      other.exceeded == exceeded &&
      other.windowDays == windowDays &&
      other.overwriteOldest == overwriteOldest;

    @override
    int get hashCode =>
        capBytes.hashCode +
        sizeBytes.hashCode +
        exceeded.hashCode +
        (windowDays == null ? 0 : windowDays.hashCode) +
        overwriteOldest.hashCode;

  factory RetentionPreviewDbCap.fromJson(Map<String, dynamic> json) => _$RetentionPreviewDbCapFromJson(json);

  Map<String, dynamic> toJson() => _$RetentionPreviewDbCapToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

