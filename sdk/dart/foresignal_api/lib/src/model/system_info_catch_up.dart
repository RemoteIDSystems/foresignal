//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'system_info_catch_up.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SystemInfoCatchUp {
  /// Returns a new [SystemInfoCatchUp] instance.
  SystemInfoCatchUp({

     this.triggerLagSeq,

     this.ingestQueueDepth,

     this.catchingUp,
  });

  @JsonKey(
    
    name: r'triggerLagSeq',
    required: false,
    includeIfNull: false,
  )


  final int? triggerLagSeq;



  @JsonKey(
    
    name: r'ingestQueueDepth',
    required: false,
    includeIfNull: false,
  )


  final int? ingestQueueDepth;



      /// lag > 10,000 seq or ingest queue > 80% for > 1 min
  @JsonKey(
    
    name: r'catchingUp',
    required: false,
    includeIfNull: false,
  )


  final bool? catchingUp;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SystemInfoCatchUp &&
      other.triggerLagSeq == triggerLagSeq &&
      other.ingestQueueDepth == ingestQueueDepth &&
      other.catchingUp == catchingUp;

    @override
    int get hashCode =>
        triggerLagSeq.hashCode +
        ingestQueueDepth.hashCode +
        catchingUp.hashCode;

  factory SystemInfoCatchUp.fromJson(Map<String, dynamic> json) => _$SystemInfoCatchUpFromJson(json);

  Map<String, dynamic> toJson() => _$SystemInfoCatchUpToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

