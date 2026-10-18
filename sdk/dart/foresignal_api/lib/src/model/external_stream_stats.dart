//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'external_stream_stats.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExternalStreamStats {
  /// Returns a new [ExternalStreamStats] instance.
  ExternalStreamStats({

     this.streamId,

     this.mode,

     this.kind,

     this.items,

     this.rejected,

     this.lastItemAt,

     this.bindings,
  });

  @JsonKey(
    
    name: r'streamId',
    required: false,
    includeIfNull: false,
  )


  final String? streamId;



  @JsonKey(
    
    name: r'mode',
    required: false,
    includeIfNull: false,
  )


  final String? mode;



  @JsonKey(
    
    name: r'kind',
    required: false,
    includeIfNull: false,
  )


  final String? kind;



  @JsonKey(
    
    name: r'items',
    required: false,
    includeIfNull: false,
  )


  final int? items;



  @JsonKey(
    
    name: r'rejected',
    required: false,
    includeIfNull: false,
  )


  final int? rejected;



  @JsonKey(
    
    name: r'lastItemAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastItemAt;



  @JsonKey(
    
    name: r'bindings',
    required: false,
    includeIfNull: false,
  )


  final int? bindings;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExternalStreamStats &&
      other.streamId == streamId &&
      other.mode == mode &&
      other.kind == kind &&
      other.items == items &&
      other.rejected == rejected &&
      other.lastItemAt == lastItemAt &&
      other.bindings == bindings;

    @override
    int get hashCode =>
        streamId.hashCode +
        mode.hashCode +
        kind.hashCode +
        items.hashCode +
        rejected.hashCode +
        (lastItemAt == null ? 0 : lastItemAt.hashCode) +
        bindings.hashCode;

  factory ExternalStreamStats.fromJson(Map<String, dynamic> json) => _$ExternalStreamStatsFromJson(json);

  Map<String, dynamic> toJson() => _$ExternalStreamStatsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

