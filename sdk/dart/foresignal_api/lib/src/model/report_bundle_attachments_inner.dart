//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'report_bundle_attachments_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReportBundleAttachmentsInner {
  /// Returns a new [ReportBundleAttachmentsInner] instance.
  ReportBundleAttachmentsInner({

    required  this.sha256,

     this.filename,

     this.contentType,

     this.size,

     this.attachmentId,
  });

  @JsonKey(
    
    name: r'sha256',
    required: true,
    includeIfNull: false,
  )


  final String sha256;



  @JsonKey(
    
    name: r'filename',
    required: false,
    includeIfNull: false,
  )


  final String? filename;



  @JsonKey(
    
    name: r'contentType',
    required: false,
    includeIfNull: false,
  )


  final String? contentType;



  @JsonKey(
    
    name: r'size',
    required: false,
    includeIfNull: false,
  )


  final int? size;



      /// ULID identifier
  @JsonKey(
    
    name: r'attachmentId',
    required: false,
    includeIfNull: false,
  )


  final String? attachmentId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReportBundleAttachmentsInner &&
      other.sha256 == sha256 &&
      other.filename == filename &&
      other.contentType == contentType &&
      other.size == size &&
      other.attachmentId == attachmentId;

    @override
    int get hashCode =>
        sha256.hashCode +
        filename.hashCode +
        contentType.hashCode +
        size.hashCode +
        attachmentId.hashCode;

  factory ReportBundleAttachmentsInner.fromJson(Map<String, dynamic> json) => _$ReportBundleAttachmentsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ReportBundleAttachmentsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

