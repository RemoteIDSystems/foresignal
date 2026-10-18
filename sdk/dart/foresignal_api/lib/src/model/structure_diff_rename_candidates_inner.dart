//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'structure_diff_rename_candidates_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class StructureDiffRenameCandidatesInner {
  /// Returns a new [StructureDiffRenameCandidatesInner] instance.
  StructureDiffRenameCandidatesInner({

    required  this.from,

    required  this.to,

    required  this.score,

    required  this.reason,

     this.readByParser,
  });

  @JsonKey(
    
    name: r'from',
    required: true,
    includeIfNull: false,
  )


  final String from;



  @JsonKey(
    
    name: r'to',
    required: true,
    includeIfNull: false,
  )


  final String to;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'score',
    required: true,
    includeIfNull: false,
  )


  final num score;



  @JsonKey(
    
    name: r'reason',
    required: true,
    includeIfNull: false,
  )


  final StructureDiffRenameCandidatesInnerReasonEnum reason;



  @JsonKey(
    
    name: r'readByParser',
    required: false,
    includeIfNull: false,
  )


  final bool? readByParser;





    @override
    bool operator ==(Object other) => identical(this, other) || other is StructureDiffRenameCandidatesInner &&
      other.from == from &&
      other.to == to &&
      other.score == score &&
      other.reason == reason &&
      other.readByParser == readByParser;

    @override
    int get hashCode =>
        from.hashCode +
        to.hashCode +
        score.hashCode +
        reason.hashCode +
        readByParser.hashCode;

  factory StructureDiffRenameCandidatesInner.fromJson(Map<String, dynamic> json) => _$StructureDiffRenameCandidatesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$StructureDiffRenameCandidatesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum StructureDiffRenameCandidatesInnerReasonEnum {
@JsonValue(r'name-similarity')
nameSimilarity(r'name-similarity'),
@JsonValue(r'snake-camel')
snakeCamel(r'snake-camel'),
@JsonValue(r'value-distribution')
valueDistribution(r'value-distribution');

const StructureDiffRenameCandidatesInnerReasonEnum(this.value);

final String value;

@override
String toString() => value;
}


