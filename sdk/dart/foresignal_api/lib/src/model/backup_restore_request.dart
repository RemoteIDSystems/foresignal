//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'backup_restore_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class BackupRestoreRequest {
  /// Returns a new [BackupRestoreRequest] instance.
  BackupRestoreRequest({

    required  this.confirm,
  });

      /// Must be true to restore
  @JsonKey(
    
    name: r'confirm',
    required: true,
    includeIfNull: false,
  )


  final bool confirm;





    @override
    bool operator ==(Object other) => identical(this, other) || other is BackupRestoreRequest &&
      other.confirm == confirm;

    @override
    int get hashCode =>
        confirm.hashCode;

  factory BackupRestoreRequest.fromJson(Map<String, dynamic> json) => _$BackupRestoreRequestFromJson(json);

  Map<String, dynamic> toJson() => _$BackupRestoreRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

