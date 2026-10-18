//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'overland_ack.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OverlandAck {
  /// Returns a new [OverlandAck] instance.
  OverlandAck({

    required  this.result,
  });

  @JsonKey(
    
    name: r'result',
    required: true,
    includeIfNull: false,
  )


  final OverlandAckResultEnum result;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OverlandAck &&
      other.result == result;

    @override
    int get hashCode =>
        result.hashCode;

  factory OverlandAck.fromJson(Map<String, dynamic> json) => _$OverlandAckFromJson(json);

  Map<String, dynamic> toJson() => _$OverlandAckToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum OverlandAckResultEnum {
@JsonValue(r'ok')
ok(r'ok');

const OverlandAckResultEnum(this.value);

final String value;

@override
String toString() => value;
}


