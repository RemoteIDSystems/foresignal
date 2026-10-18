//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/situation_join_reason_components.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'situation_join_reason.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SituationJoinReason {
  /// Returns a new [SituationJoinReason] instance.
  SituationJoinReason({

    required  this.method,

     this.score,

     this.components,

     this.linkerVersion,

     this.actor,
  });

      /// How the link was made
  @JsonKey(
    
    name: r'method',
    required: true,
    includeIfNull: false,
  )


  final SituationJoinReasonMethodEnum method;



      /// Linker score in [0, 1] (null for manual links)
          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'score',
    required: false,
    includeIfNull: false,
  )


  final num? score;



  @JsonKey(
    
    name: r'components',
    required: false,
    includeIfNull: false,
  )


  final SituationJoinReasonComponents? components;



      /// Linker version, e.g. `1.0.0`
  @JsonKey(
    
    name: r'linkerVersion',
    required: false,
    includeIfNull: false,
  )


  final String? linkerVersion;



      /// Token id, `linker:<version>` or `analyst:<profile>`
  @JsonKey(
    
    name: r'actor',
    required: false,
    includeIfNull: false,
  )


  final String? actor;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SituationJoinReason &&
      other.method == method &&
      other.score == score &&
      other.components == components &&
      other.linkerVersion == linkerVersion &&
      other.actor == actor;

    @override
    int get hashCode =>
        method.hashCode +
        (score == null ? 0 : score.hashCode) +
        components.hashCode +
        (linkerVersion == null ? 0 : linkerVersion.hashCode) +
        (actor == null ? 0 : actor.hashCode);

  factory SituationJoinReason.fromJson(Map<String, dynamic> json) => _$SituationJoinReasonFromJson(json);

  Map<String, dynamic> toJson() => _$SituationJoinReasonToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// How the link was made
enum SituationJoinReasonMethodEnum {
@JsonValue(r'linker')
linker(r'linker'),
@JsonValue(r'seed')
seed(r'seed'),
@JsonValue(r'operator')
operator_(r'operator'),
@JsonValue(r'agent')
agent(r'agent'),
@JsonValue(r'analyst')
analyst(r'analyst');

const SituationJoinReasonMethodEnum(this.value);

final String value;

@override
String toString() => value;
}


