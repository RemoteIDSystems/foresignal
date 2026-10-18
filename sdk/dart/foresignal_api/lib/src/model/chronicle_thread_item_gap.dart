//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_thread_item_gap.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleThreadItemGap {
  /// Returns a new [ChronicleThreadItemGap] instance.
  ChronicleThreadItemGap({

    required  this.from,

    required  this.to,

     this.reason,
  });

  @JsonKey(
    
    name: r'from',
    required: true,
    includeIfNull: false,
  )


  final DateTime from;



  @JsonKey(
    
    name: r'to',
    required: true,
    includeIfNull: false,
  )


  final DateTime to;



  @JsonKey(
    
    name: r'reason',
    required: false,
    includeIfNull: false,
  )


  final String? reason;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleThreadItemGap &&
      other.from == from &&
      other.to == to &&
      other.reason == reason;

    @override
    int get hashCode =>
        from.hashCode +
        to.hashCode +
        reason.hashCode;

  factory ChronicleThreadItemGap.fromJson(Map<String, dynamic> json) => _$ChronicleThreadItemGapFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleThreadItemGapToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

