//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'intel_board_indicators_observed_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IntelBoardIndicatorsObservedInner {
  /// Returns a new [IntelBoardIndicatorsObservedInner] instance.
  IntelBoardIndicatorsObservedInner({

    required  this.indicatorId,

    required  this.setId,

     this.description,

    required  this.at,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'indicatorId',
    required: true,
    includeIfNull: false,
  )


  final String indicatorId;



      /// ULID identifier
  @JsonKey(
    
    name: r'setId',
    required: true,
    includeIfNull: false,
  )


  final String setId;



  @JsonKey(
    
    name: r'description',
    required: false,
    includeIfNull: false,
  )


  final String? description;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IntelBoardIndicatorsObservedInner &&
      other.indicatorId == indicatorId &&
      other.setId == setId &&
      other.description == description &&
      other.at == at;

    @override
    int get hashCode =>
        indicatorId.hashCode +
        setId.hashCode +
        description.hashCode +
        at.hashCode;

  factory IntelBoardIndicatorsObservedInner.fromJson(Map<String, dynamic> json) => _$IntelBoardIndicatorsObservedInnerFromJson(json);

  Map<String, dynamic> toJson() => _$IntelBoardIndicatorsObservedInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

