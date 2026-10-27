//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/retention_preview_item.dart';
import 'package:foresignal_api/src/model/retention_preview_db_cap.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'retention_preview.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RetentionPreview {
  /// Returns a new [RetentionPreview] instance.
  RetentionPreview({

    required  this.items,

    required  this.totalAffected,

     this.totalBytesEstimate,

    required  this.generatedAt,

     this.dbCap,
  });

  @JsonKey(
    
    name: r'items',
    required: true,
    includeIfNull: false,
  )


  final List<RetentionPreviewItem> items;



      /// Sum of the items' counts
  @JsonKey(
    
    name: r'totalAffected',
    required: true,
    includeIfNull: false,
  )


  final int totalAffected;



      /// Sum of the items' byte estimates
  @JsonKey(
    
    name: r'totalBytesEstimate',
    required: false,
    includeIfNull: false,
  )


  final int? totalBytesEstimate;



  @JsonKey(
    
    name: r'generatedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime generatedAt;



  @JsonKey(
    
    name: r'dbCap',
    required: false,
    includeIfNull: false,
  )


  final RetentionPreviewDbCap? dbCap;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RetentionPreview &&
      other.items == items &&
      other.totalAffected == totalAffected &&
      other.totalBytesEstimate == totalBytesEstimate &&
      other.generatedAt == generatedAt &&
      other.dbCap == dbCap;

    @override
    int get hashCode =>
        items.hashCode +
        totalAffected.hashCode +
        (totalBytesEstimate == null ? 0 : totalBytesEstimate.hashCode) +
        generatedAt.hashCode +
        (dbCap == null ? 0 : dbCap.hashCode);

  factory RetentionPreview.fromJson(Map<String, dynamic> json) => _$RetentionPreviewFromJson(json);

  Map<String, dynamic> toJson() => _$RetentionPreviewToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

