//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'received_package_contents_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReceivedPackageContentsInner {
  /// Returns a new [ReceivedPackageContentsInner] instance.
  ReceivedPackageContentsInner({

    required  this.path,

    required  this.sha256,

     this.size,

     this.mediaId,
  });

  @JsonKey(
    
    name: r'path',
    required: true,
    includeIfNull: false,
  )


  final String path;



  @JsonKey(
    
    name: r'sha256',
    required: true,
    includeIfNull: false,
  )


  final String sha256;



  @JsonKey(
    
    name: r'size',
    required: false,
    includeIfNull: false,
  )


  final int? size;



  @JsonKey(
    
    name: r'mediaId',
    required: false,
    includeIfNull: false,
  )


  final String? mediaId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReceivedPackageContentsInner &&
      other.path == path &&
      other.sha256 == sha256 &&
      other.size == size &&
      other.mediaId == mediaId;

    @override
    int get hashCode =>
        path.hashCode +
        sha256.hashCode +
        size.hashCode +
        (mediaId == null ? 0 : mediaId.hashCode);

  factory ReceivedPackageContentsInner.fromJson(Map<String, dynamic> json) => _$ReceivedPackageContentsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ReceivedPackageContentsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

