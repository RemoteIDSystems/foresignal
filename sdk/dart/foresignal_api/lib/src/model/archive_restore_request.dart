//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'archive_restore_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ArchiveRestoreRequest {
  /// Returns a new [ArchiveRestoreRequest] instance.
  ArchiveRestoreRequest({

    required  this.confirm,
  });

      /// Must be true to restore (400 otherwise): a restore may be large and slow, and a Glacier segment waits hours for its thaw
  @JsonKey(
    
    name: r'confirm',
    required: true,
    includeIfNull: false,
  )


  final bool confirm;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ArchiveRestoreRequest &&
      other.confirm == confirm;

    @override
    int get hashCode =>
        confirm.hashCode;

  factory ArchiveRestoreRequest.fromJson(Map<String, dynamic> json) => _$ArchiveRestoreRequestFromJson(json);

  Map<String, dynamic> toJson() => _$ArchiveRestoreRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

