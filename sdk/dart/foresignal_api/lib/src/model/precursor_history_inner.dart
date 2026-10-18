//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'precursor_history_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PrecursorHistoryInner {
  /// Returns a new [PrecursorHistoryInner] instance.
  PrecursorHistoryInner({

    required  this.at,

    required  this.by,

     this.from,

    required  this.to,

     this.reason,
  });

  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;



  @JsonKey(
    
    name: r'by',
    required: true,
    includeIfNull: false,
  )


  final String by;



  @JsonKey(
    
    name: r'from',
    required: false,
    includeIfNull: false,
  )


  final String? from;



  @JsonKey(
    
    name: r'to',
    required: true,
    includeIfNull: false,
  )


  final String to;



  @JsonKey(
    
    name: r'reason',
    required: false,
    includeIfNull: false,
  )


  final String? reason;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PrecursorHistoryInner &&
      other.at == at &&
      other.by == by &&
      other.from == from &&
      other.to == to &&
      other.reason == reason;

    @override
    int get hashCode =>
        at.hashCode +
        by.hashCode +
        (from == null ? 0 : from.hashCode) +
        to.hashCode +
        (reason == null ? 0 : reason.hashCode);

  factory PrecursorHistoryInner.fromJson(Map<String, dynamic> json) => _$PrecursorHistoryInnerFromJson(json);

  Map<String, dynamic> toJson() => _$PrecursorHistoryInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

