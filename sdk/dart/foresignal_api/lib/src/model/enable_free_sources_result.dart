//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'enable_free_sources_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EnableFreeSourcesResult {
  /// Returns a new [EnableFreeSourcesResult] instance.
  EnableFreeSourcesResult({

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



      /// Disabled built-in sources left disabled: they need a credential or a required parameter, are held off by default (`wiki-edit-surge`), or their stored parameters are not valid
          // minimum: 0
  @JsonKey(
    
    name: r'skipped',
    required: true,
    includeIfNull: false,
  )


  final int skipped;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EnableFreeSourcesResult &&
      other.enabled == enabled &&
      other.count == count &&
      other.skipped == skipped;

    @override
    int get hashCode =>
        enabled.hashCode +
        count.hashCode +
        skipped.hashCode;

  factory EnableFreeSourcesResult.fromJson(Map<String, dynamic> json) => _$EnableFreeSourcesResultFromJson(json);

  Map<String, dynamic> toJson() => _$EnableFreeSourcesResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

