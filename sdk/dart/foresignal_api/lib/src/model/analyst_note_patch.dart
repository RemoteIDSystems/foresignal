//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analyst_note_patch.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalystNotePatch {
  /// Returns a new [AnalystNotePatch] instance.
  AnalystNotePatch({

    required  this.status,
  });

  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final AnalystNotePatchStatusEnum status;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalystNotePatch &&
      other.status == status;

    @override
    int get hashCode =>
        status.hashCode;

  factory AnalystNotePatch.fromJson(Map<String, dynamic> json) => _$AnalystNotePatchFromJson(json);

  Map<String, dynamic> toJson() => _$AnalystNotePatchToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AnalystNotePatchStatusEnum {
@JsonValue(r'new')
new_(r'new'),
@JsonValue(r'read')
read(r'read'),
@JsonValue(r'dismissed')
dismissed(r'dismissed');

const AnalystNotePatchStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


