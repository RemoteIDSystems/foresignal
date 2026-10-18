//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'information_value_indicators_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class InformationValueIndicatorsInner {
  /// Returns a new [InformationValueIndicatorsInner] instance.
  InformationValueIndicatorsInner({

    required  this.indicatorId,

     this.description,

     this.signalFamily,

     this.state,

    required  this.rank,

    required  this.eig,

    required  this.potentialEig,

     this.pDetect,

     this.pObserved,

     this.collectionGap,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'indicatorId',
    required: true,
    includeIfNull: false,
  )


  final String indicatorId;



  @JsonKey(
    
    name: r'description',
    required: false,
    includeIfNull: false,
  )


  final String? description;



  @JsonKey(
    
    name: r'signalFamily',
    required: false,
    includeIfNull: false,
  )


  final String? signalFamily;



  @JsonKey(
    
    name: r'state',
    required: false,
    includeIfNull: false,
  )


  final InformationValueIndicatorsInnerStateEnum? state;



  @JsonKey(
    
    name: r'rank',
    required: true,
    includeIfNull: false,
  )


  final int rank;



  @JsonKey(
    
    name: r'eig',
    required: true,
    includeIfNull: false,
  )


  final num eig;



      /// EIG with p_detect = 1
  @JsonKey(
    
    name: r'potentialEig',
    required: true,
    includeIfNull: false,
  )


  final num potentialEig;



  @JsonKey(
    
    name: r'pDetect',
    required: false,
    includeIfNull: false,
  )


  final num? pDetect;



  @JsonKey(
    
    name: r'pObserved',
    required: false,
    includeIfNull: false,
  )


  final num? pObserved;



  @JsonKey(
    
    name: r'collectionGap',
    required: false,
    includeIfNull: false,
  )


  final bool? collectionGap;





    @override
    bool operator ==(Object other) => identical(this, other) || other is InformationValueIndicatorsInner &&
      other.indicatorId == indicatorId &&
      other.description == description &&
      other.signalFamily == signalFamily &&
      other.state == state &&
      other.rank == rank &&
      other.eig == eig &&
      other.potentialEig == potentialEig &&
      other.pDetect == pDetect &&
      other.pObserved == pObserved &&
      other.collectionGap == collectionGap;

    @override
    int get hashCode =>
        indicatorId.hashCode +
        description.hashCode +
        signalFamily.hashCode +
        state.hashCode +
        rank.hashCode +
        eig.hashCode +
        potentialEig.hashCode +
        (pDetect == null ? 0 : pDetect.hashCode) +
        (pObserved == null ? 0 : pObserved.hashCode) +
        collectionGap.hashCode;

  factory InformationValueIndicatorsInner.fromJson(Map<String, dynamic> json) => _$InformationValueIndicatorsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$InformationValueIndicatorsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum InformationValueIndicatorsInnerStateEnum {
@JsonValue(r'pending')
pending(r'pending'),
@JsonValue(r'emerging')
emerging(r'emerging'),
@JsonValue(r'observed')
observed(r'observed'),
@JsonValue(r'absent')
absent(r'absent'),
@JsonValue(r'unknown-coverage')
unknownCoverage(r'unknown-coverage'),
@JsonValue(r'retired')
retired(r'retired'),
@JsonValue(r'reported')
reported(r'reported');

const InformationValueIndicatorsInnerStateEnum(this.value);

final String value;

@override
String toString() => value;
}


