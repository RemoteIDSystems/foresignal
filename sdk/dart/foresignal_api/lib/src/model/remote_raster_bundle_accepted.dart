//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'remote_raster_bundle_accepted.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RemoteRasterBundleAccepted {
  /// Returns a new [RemoteRasterBundleAccepted] instance.
  RemoteRasterBundleAccepted({

    required  this.uploadId,

    required  this.passFolder,
  });

  @JsonKey(
    
    name: r'uploadId',
    required: true,
    includeIfNull: false,
  )


  final String uploadId;



  @JsonKey(
    
    name: r'passFolder',
    required: true,
    includeIfNull: false,
  )


  final String passFolder;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RemoteRasterBundleAccepted &&
      other.uploadId == uploadId &&
      other.passFolder == passFolder;

    @override
    int get hashCode =>
        uploadId.hashCode +
        passFolder.hashCode;

  factory RemoteRasterBundleAccepted.fromJson(Map<String, dynamic> json) => _$RemoteRasterBundleAcceptedFromJson(json);

  Map<String, dynamic> toJson() => _$RemoteRasterBundleAcceptedToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

