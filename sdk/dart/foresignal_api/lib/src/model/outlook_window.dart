//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/quantiles.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'outlook_window.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OutlookWindow {
  /// Returns a new [OutlookWindow] instance.
  OutlookWindow({

    required  this.index,

    required  this.windowStart,

    required  this.windowEnd,

    required  this.quantiles,

     this.questionId,

    required  this.status,

     this.actual,

     this.withinP10P90,

     this.exceedance,
  });

          // minimum: 1
          // maximum: 6
  @JsonKey(
    
    name: r'index',
    required: true,
    includeIfNull: false,
  )


  final int index;



  @JsonKey(
    
    name: r'windowStart',
    required: true,
    includeIfNull: false,
  )


  final DateTime windowStart;



  @JsonKey(
    
    name: r'windowEnd',
    required: true,
    includeIfNull: false,
  )


  final DateTime windowEnd;



  @JsonKey(
    
    name: r'quantiles',
    required: true,
    includeIfNull: false,
  )


  final Quantiles quantiles;



      /// ULID identifier
  @JsonKey(
    
    name: r'questionId',
    required: false,
    includeIfNull: false,
  )


  final String? questionId;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final OutlookWindowStatusEnum status;



  @JsonKey(
    
    name: r'actual',
    required: false,
    includeIfNull: false,
  )


  final int? actual;



  @JsonKey(
    
    name: r'withinP10P90',
    required: false,
    includeIfNull: false,
  )


  final bool? withinP10P90;



  @JsonKey(
    
    name: r'exceedance',
    required: false,
    includeIfNull: false,
  )


  final OutlookWindowExceedanceEnum? exceedance;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OutlookWindow &&
      other.index == index &&
      other.windowStart == windowStart &&
      other.windowEnd == windowEnd &&
      other.quantiles == quantiles &&
      other.questionId == questionId &&
      other.status == status &&
      other.actual == actual &&
      other.withinP10P90 == withinP10P90 &&
      other.exceedance == exceedance;

    @override
    int get hashCode =>
        index.hashCode +
        windowStart.hashCode +
        windowEnd.hashCode +
        quantiles.hashCode +
        (questionId == null ? 0 : questionId.hashCode) +
        status.hashCode +
        (actual == null ? 0 : actual.hashCode) +
        (withinP10P90 == null ? 0 : withinP10P90.hashCode) +
        (exceedance == null ? 0 : exceedance.hashCode);

  factory OutlookWindow.fromJson(Map<String, dynamic> json) => _$OutlookWindowFromJson(json);

  Map<String, dynamic> toJson() => _$OutlookWindowToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum OutlookWindowStatusEnum {
@JsonValue(r'open')
open(r'open'),
@JsonValue(r'resolved')
resolved(r'resolved'),
@JsonValue(r'annulled')
annulled(r'annulled');

const OutlookWindowStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


enum OutlookWindowExceedanceEnum {
@JsonValue(r'above-p95')
aboveP95(r'above-p95'),
@JsonValue(r'below-p05')
belowP05(r'below-p05');

const OutlookWindowExceedanceEnum(this.value);

final String value;

@override
String toString() => value;
}


