//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'index_weights_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IndexWeightsInput {
  /// Returns a new [IndexWeightsInput] instance.
  IndexWeightsInput({

    required  this.source_,

     this.version,

     this.weights,

     this.rationale,
  });

  @JsonKey(
    
    name: r'source',
    required: true,
    includeIfNull: false,
  )


  final IndexWeightsInputSource_Enum source_;



      /// Fitted weights version (required for `fitted`)
  @JsonKey(
    
    name: r'version',
    required: false,
    includeIfNull: false,
  )


  final int? version;



      /// Required for `operator`
  @JsonKey(
    
    name: r'weights',
    required: false,
    includeIfNull: false,
  )


  final Map<String, num>? weights;



  @JsonKey(
    
    name: r'rationale',
    required: false,
    includeIfNull: false,
  )


  final String? rationale;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IndexWeightsInput &&
      other.source_ == source_ &&
      other.version == version &&
      other.weights == weights &&
      other.rationale == rationale;

    @override
    int get hashCode =>
        source_.hashCode +
        version.hashCode +
        weights.hashCode +
        (rationale == null ? 0 : rationale.hashCode);

  factory IndexWeightsInput.fromJson(Map<String, dynamic> json) => _$IndexWeightsInputFromJson(json);

  Map<String, dynamic> toJson() => _$IndexWeightsInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum IndexWeightsInputSource_Enum {
@JsonValue(r'fitted')
fitted(r'fitted'),
@JsonValue(r'operator')
operator_(r'operator'),
@JsonValue(r'default')
default_(r'default');

const IndexWeightsInputSource_Enum(this.value);

final String value;

@override
String toString() => value;
}


