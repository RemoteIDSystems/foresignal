//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'mapping_warning.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MappingWarning {
  /// Returns a new [MappingWarning] instance.
  MappingWarning({

    required  this.code,

    required  this.message,

     this.path,
  });

  @JsonKey(
    
    name: r'code',
    required: true,
    includeIfNull: false,
  )


  final String code;



  @JsonKey(
    
    name: r'message',
    required: true,
    includeIfNull: false,
  )


  final String message;



  @JsonKey(
    
    name: r'path',
    required: false,
    includeIfNull: false,
  )


  final String? path;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MappingWarning &&
      other.code == code &&
      other.message == message &&
      other.path == path;

    @override
    int get hashCode =>
        code.hashCode +
        message.hashCode +
        (path == null ? 0 : path.hashCode);

  factory MappingWarning.fromJson(Map<String, dynamic> json) => _$MappingWarningFromJson(json);

  Map<String, dynamic> toJson() => _$MappingWarningToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

