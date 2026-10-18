//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'retention_run_result_consumer_clamp.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RetentionRunResultConsumerClamp {
  /// Returns a new [RetentionRunResultConsumerClamp] instance.
  RetentionRunResultConsumerClamp({

     this.active,

     this.consumer,

     this.cursorSeq,
  });

  @JsonKey(
    
    name: r'active',
    required: false,
    includeIfNull: false,
  )


  final bool? active;



  @JsonKey(
    
    name: r'consumer',
    required: false,
    includeIfNull: false,
  )


  final String? consumer;



  @JsonKey(
    
    name: r'cursorSeq',
    required: false,
    includeIfNull: false,
  )


  final int? cursorSeq;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RetentionRunResultConsumerClamp &&
      other.active == active &&
      other.consumer == consumer &&
      other.cursorSeq == cursorSeq;

    @override
    int get hashCode =>
        active.hashCode +
        (consumer == null ? 0 : consumer.hashCode) +
        (cursorSeq == null ? 0 : cursorSeq.hashCode);

  factory RetentionRunResultConsumerClamp.fromJson(Map<String, dynamic> json) => _$RetentionRunResultConsumerClampFromJson(json);

  Map<String, dynamic> toJson() => _$RetentionRunResultConsumerClampToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

