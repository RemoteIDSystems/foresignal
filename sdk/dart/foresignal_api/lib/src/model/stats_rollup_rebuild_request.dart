//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'stats_rollup_rebuild_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class StatsRollupRebuildRequest {
  /// Returns a new [StatsRollupRebuildRequest] instance.
  StatsRollupRebuildRequest({

    required  this.from,

    required  this.to,
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





    @override
    bool operator ==(Object other) => identical(this, other) || other is StatsRollupRebuildRequest &&
      other.from == from &&
      other.to == to;

    @override
    int get hashCode =>
        from.hashCode +
        to.hashCode;

  factory StatsRollupRebuildRequest.fromJson(Map<String, dynamic> json) => _$StatsRollupRebuildRequestFromJson(json);

  Map<String, dynamic> toJson() => _$StatsRollupRebuildRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

