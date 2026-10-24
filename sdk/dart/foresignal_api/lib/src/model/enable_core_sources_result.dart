//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'enable_core_sources_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EnableCoreSourcesResult {
  /// Returns a new [EnableCoreSourcesResult] instance.
  EnableCoreSourcesResult({

    required  this.enabled,

    required  this.count,

    required  this.skipped,
  });

      /// The source ids enabled by this call, sorted; empty when there was nothing to enable
  @JsonKey(
    
    name: r'enabled',
    required: true,
    includeIfNull: false,
  )


  final List<String> enabled;



      /// How many sources this call enabled (the length of `enabled`)
          // minimum: 0
  @JsonKey(
    
    name: r'count',
    required: true,
    includeIfNull: false,
  )


  final int count;



      /// Disabled built-in sources left disabled: outside the core set, a core source that needs a credential or a required parameter (`nasa-firms` without its MAP_KEY), or one whose stored parameters are not valid
          // minimum: 0
  @JsonKey(
    
    name: r'skipped',
    required: true,
    includeIfNull: false,
  )


  final int skipped;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EnableCoreSourcesResult &&
      other.enabled == enabled &&
      other.count == count &&
      other.skipped == skipped;

    @override
    int get hashCode =>
        enabled.hashCode +
        count.hashCode +
        skipped.hashCode;

  factory EnableCoreSourcesResult.fromJson(Map<String, dynamic> json) => _$EnableCoreSourcesResultFromJson(json);

  Map<String, dynamic> toJson() => _$EnableCoreSourcesResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

