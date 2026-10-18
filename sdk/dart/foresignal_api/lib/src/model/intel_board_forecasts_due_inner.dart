//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/icd_term.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'intel_board_forecasts_due_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IntelBoardForecastsDueInner {
  /// Returns a new [IntelBoardForecastsDueInner] instance.
  IntelBoardForecastsDueInner({

    required  this.questionId,

    required  this.title,

    required  this.dueAt,

     this.probability,

     this.estimativeTerm,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'questionId',
    required: true,
    includeIfNull: false,
  )


  final String questionId;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



  @JsonKey(
    
    name: r'dueAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime dueAt;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'probability',
    required: false,
    includeIfNull: false,
  )


  final num? probability;



  @JsonKey(
    
    name: r'estimativeTerm',
    required: false,
    includeIfNull: false,
  )


  final IcdTerm? estimativeTerm;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IntelBoardForecastsDueInner &&
      other.questionId == questionId &&
      other.title == title &&
      other.dueAt == dueAt &&
      other.probability == probability &&
      other.estimativeTerm == estimativeTerm;

    @override
    int get hashCode =>
        questionId.hashCode +
        title.hashCode +
        dueAt.hashCode +
        (probability == null ? 0 : probability.hashCode) +
        (estimativeTerm == null ? 0 : estimativeTerm.hashCode);

  factory IntelBoardForecastsDueInner.fromJson(Map<String, dynamic> json) => _$IntelBoardForecastsDueInnerFromJson(json);

  Map<String, dynamic> toJson() => _$IntelBoardForecastsDueInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

