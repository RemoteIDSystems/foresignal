//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/kind_display.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'kind_summary.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class KindSummary {
  /// Returns a new [KindSummary] instance.
  KindSummary({

    required  this.kind,

    required  this.currentCount,

    required  this.sources,

     this.display,
  });

  @JsonKey(
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final String kind;



  @JsonKey(
    
    name: r'currentCount',
    required: true,
    includeIfNull: false,
  )


  final int currentCount;



  @JsonKey(
    
    name: r'sources',
    required: true,
    includeIfNull: false,
  )


  final List<String> sources;



  @JsonKey(
    
    name: r'display',
    required: false,
    includeIfNull: false,
  )


  final KindDisplay? display;





    @override
    bool operator ==(Object other) => identical(this, other) || other is KindSummary &&
      other.kind == kind &&
      other.currentCount == currentCount &&
      other.sources == sources &&
      other.display == display;

    @override
    int get hashCode =>
        kind.hashCode +
        currentCount.hashCode +
        sources.hashCode +
        (display == null ? 0 : display.hashCode);

  factory KindSummary.fromJson(Map<String, dynamic> json) => _$KindSummaryFromJson(json);

  Map<String, dynamic> toJson() => _$KindSummaryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

