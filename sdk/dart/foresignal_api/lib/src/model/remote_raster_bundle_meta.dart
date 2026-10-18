//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'remote_raster_bundle_meta.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RemoteRasterBundleMeta {
  /// Returns a new [RemoteRasterBundleMeta] instance.
  RemoteRasterBundleMeta({

    required  this.passFolder,

    required  this.files,

     this.product,
  });

      /// Pass folder name: one safe path segment (no separators, no `..`, no leading dot), at most 128 characters
  @JsonKey(
    
    name: r'passFolder',
    required: true,
    includeIfNull: false,
  )


  final String passFolder;



      /// Filenames of the file parts (no separators, no `..`)
  @JsonKey(
    
    name: r'files',
    required: true,
    includeIfNull: false,
  )


  final List<String> files;



      /// Product directory for every file except `dataset.json` (one safe path segment, for example `MSU-MR`); default: the single product `dataset.json` lists, else the pass folder. Send one bundle per product for a multi-instrument pass.
  @JsonKey(
    
    name: r'product',
    required: false,
    includeIfNull: false,
  )


  final String? product;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RemoteRasterBundleMeta &&
      other.passFolder == passFolder &&
      other.files == files &&
      other.product == product;

    @override
    int get hashCode =>
        passFolder.hashCode +
        files.hashCode +
        (product == null ? 0 : product.hashCode);

  factory RemoteRasterBundleMeta.fromJson(Map<String, dynamic> json) => _$RemoteRasterBundleMetaFromJson(json);

  Map<String, dynamic> toJson() => _$RemoteRasterBundleMetaToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

