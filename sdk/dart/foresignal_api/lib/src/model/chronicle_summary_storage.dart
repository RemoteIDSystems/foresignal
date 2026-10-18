//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_summary_storage.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleSummaryStorage {
  /// Returns a new [ChronicleSummaryStorage] instance.
  ChronicleSummaryStorage({

    required  this.bytesUsed,

    required  this.sizeCapBytes,

     this.rows,
  });

  @JsonKey(
    
    name: r'bytesUsed',
    required: true,
    includeIfNull: false,
  )


  final int bytesUsed;



  @JsonKey(
    
    name: r'sizeCapBytes',
    required: true,
    includeIfNull: false,
  )


  final int sizeCapBytes;



  @JsonKey(
    
    name: r'rows',
    required: false,
    includeIfNull: false,
  )


  final int? rows;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleSummaryStorage &&
      other.bytesUsed == bytesUsed &&
      other.sizeCapBytes == sizeCapBytes &&
      other.rows == rows;

    @override
    int get hashCode =>
        bytesUsed.hashCode +
        sizeCapBytes.hashCode +
        rows.hashCode;

  factory ChronicleSummaryStorage.fromJson(Map<String, dynamic> json) => _$ChronicleSummaryStorageFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleSummaryStorageToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

