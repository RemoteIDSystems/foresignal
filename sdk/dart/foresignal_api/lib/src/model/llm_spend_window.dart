//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'llm_spend_window.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class LlmSpendWindow {
  /// Returns a new [LlmSpendWindow] instance.
  LlmSpendWindow({

    required  this.window,

    required  this.start,

    required  this.resetsAt,

    required  this.spentUsd,

    required  this.reservedUsd,

    required  this.capUsd,

    required  this.remainingUsd,

    required  this.exhausted,
  });

  @JsonKey(
    
    name: r'window',
    required: true,
    includeIfNull: false,
  )


  final LlmSpendWindowWindowEnum window;



      /// When the window began (UTC boundary)
  @JsonKey(
    
    name: r'start',
    required: true,
    includeIfNull: false,
  )


  final DateTime start;



      /// When it resets: the next hour, 00:00 UTC, or the 1st
  @JsonKey(
    
    name: r'resetsAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime resetsAt;



      /// USD spent in the window: settled calls at the provider's prices, seeded from the stored usage after a restart
  @JsonKey(
    
    name: r'spentUsd',
    required: true,
    includeIfNull: false,
  )


  final num spentUsd;



      /// Estimated USD of the calls in flight, held against the cap until they settle
  @JsonKey(
    
    name: r'reservedUsd',
    required: true,
    includeIfNull: false,
  )


  final num reservedUsd;



      /// The cap, or null when this window has none
  @JsonKey(
    
    name: r'capUsd',
    required: true,
    includeIfNull: true,
  )


  final num? capUsd;



      /// capUsd less spentUsd, never below 0; null without a cap
  @JsonKey(
    
    name: r'remainingUsd',
    required: true,
    includeIfNull: true,
  )


  final num? remainingUsd;



      /// True when spent plus reserved has reached the cap: calls are refused until resetsAt
  @JsonKey(
    
    name: r'exhausted',
    required: true,
    includeIfNull: false,
  )


  final bool exhausted;





    @override
    bool operator ==(Object other) => identical(this, other) || other is LlmSpendWindow &&
      other.window == window &&
      other.start == start &&
      other.resetsAt == resetsAt &&
      other.spentUsd == spentUsd &&
      other.reservedUsd == reservedUsd &&
      other.capUsd == capUsd &&
      other.remainingUsd == remainingUsd &&
      other.exhausted == exhausted;

    @override
    int get hashCode =>
        window.hashCode +
        start.hashCode +
        resetsAt.hashCode +
        spentUsd.hashCode +
        reservedUsd.hashCode +
        (capUsd == null ? 0 : capUsd.hashCode) +
        (remainingUsd == null ? 0 : remainingUsd.hashCode) +
        exhausted.hashCode;

  factory LlmSpendWindow.fromJson(Map<String, dynamic> json) => _$LlmSpendWindowFromJson(json);

  Map<String, dynamic> toJson() => _$LlmSpendWindowToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum LlmSpendWindowWindowEnum {
@JsonValue(r'hour')
hour(r'hour'),
@JsonValue(r'day')
day(r'day'),
@JsonValue(r'month')
month(r'month');

const LlmSpendWindowWindowEnum(this.value);

final String value;

@override
String toString() => value;
}


