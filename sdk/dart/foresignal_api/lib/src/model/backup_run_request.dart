//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'backup_run_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class BackupRunRequest {
  /// Returns a new [BackupRunRequest] instance.
  BackupRunRequest({

     this.type,

     this.label,
  });

      /// Omitted: `full` when `includeConfig` is on, else `database`
  @JsonKey(
    
    name: r'type',
    required: false,
    includeIfNull: false,
  )


  final String? type;



  @JsonKey(
    
    name: r'label',
    required: false,
    includeIfNull: false,
  )


  final String? label;





    @override
    bool operator ==(Object other) => identical(this, other) || other is BackupRunRequest &&
      other.type == type &&
      other.label == label;

    @override
    int get hashCode =>
        type.hashCode +
        (label == null ? 0 : label.hashCode);

  factory BackupRunRequest.fromJson(Map<String, dynamic> json) => _$BackupRunRequestFromJson(json);

  Map<String, dynamic> toJson() => _$BackupRunRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

