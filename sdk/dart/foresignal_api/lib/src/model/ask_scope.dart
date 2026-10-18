//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ask_scope.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AskScope {
  /// Returns a new [AskScope] instance.
  AskScope({

     this.sources,

     this.kinds,

     this.bbox,

     this.since,

     this.until,

     this.placeIds,

     this.viewId,
  });

  @JsonKey(
    
    name: r'sources',
    required: false,
    includeIfNull: false,
  )


  final List<String>? sources;



  @JsonKey(
    
    name: r'kinds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? kinds;



  @JsonKey(
    
    name: r'bbox',
    required: false,
    includeIfNull: false,
  )


  final String? bbox;



  @JsonKey(
    
    name: r'since',
    required: false,
    includeIfNull: false,
  )


  final DateTime? since;



  @JsonKey(
    
    name: r'until',
    required: false,
    includeIfNull: false,
  )


  final DateTime? until;



      /// Places (036) resolved server-side to areas; the model sees names and rounded distances only. Available after prompt 036: until then a scope with placeIds is refused with 400.
  @JsonKey(
    
    name: r'placeIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? placeIds;



      /// Saved view id or slug (036) whose predicate is injected
  @JsonKey(
    
    name: r'viewId',
    required: false,
    includeIfNull: false,
  )


  final String? viewId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AskScope &&
      other.sources == sources &&
      other.kinds == kinds &&
      other.bbox == bbox &&
      other.since == since &&
      other.until == until &&
      other.placeIds == placeIds &&
      other.viewId == viewId;

    @override
    int get hashCode =>
        sources.hashCode +
        kinds.hashCode +
        (bbox == null ? 0 : bbox.hashCode) +
        (since == null ? 0 : since.hashCode) +
        (until == null ? 0 : until.hashCode) +
        placeIds.hashCode +
        (viewId == null ? 0 : viewId.hashCode);

  factory AskScope.fromJson(Map<String, dynamic> json) => _$AskScopeFromJson(json);

  Map<String, dynamic> toJson() => _$AskScopeToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

