#pragma once

#include "pathfinder/Node.hpp"
#include <vector>
#include <stdexcept>

namespace mc {

class BinaryHeap {
public:
    BinaryHeap() {
        heap.reserve(1024);
    }

    Node* insert(Node* node) {
        if (node->heapIdx >= 0) {
            throw std::runtime_error("Node already in heap");
        }

        heap.push_back(node);
        node->heapIdx = static_cast<int>(heap.size()) - 1;
        upHeap(node->heapIdx);
        return node;
    }

    void clear() {
        for (Node* n : heap) {
            if (n) n->heapIdx = -1;
        }
        heap.clear();
    }

    Node* peek() const {
        return heap.empty() ? nullptr : heap[0];
    }

    Node* pop() {
        if (heap.empty()) return nullptr;

        Node* result = heap[0];
        heap[0] = heap.back();
        heap[0]->heapIdx = 0;
        heap.pop_back();

        if (!heap.empty()) {
            downHeap(0);
        }

        result->heapIdx = -1;
        return result;
    }

    void changeCost(Node* node, float newCost) {
        float oldCost = node->f;
        node->f = newCost;
        if (newCost < oldCost) {
            upHeap(node->heapIdx);
        } else {
            downHeap(node->heapIdx);
        }
    }

    bool isEmpty() const {
        return heap.empty();
    }

    int size() const {
        return static_cast<int>(heap.size());
    }

private:
    void upHeap(int idx) {
        Node* node = heap[idx];
        float cost = node->f;

        while (idx > 0) {
            int parent = (idx - 1) >> 1;
            Node* parentNode = heap[parent];
            if (cost >= parentNode->f) break;

            heap[idx] = parentNode;
            parentNode->heapIdx = idx;
            idx = parent;
        }

        heap[idx] = node;
        node->heapIdx = idx;
    }

    void downHeap(int idx) {
        Node* node = heap[idx];
        float cost = node->f;
        int size = static_cast<int>(heap.size());

        while (true) {
            int left = 1 + (idx << 1);
            int right = left + 1;

            if (left >= size) break;

            Node* leftNode = heap[left];
            float leftCost = leftNode->f;

            Node* rightNode = (right < size) ? heap[right] : nullptr;
            float rightCost = rightNode ? rightNode->f : std::numeric_limits<float>::infinity();

            if (leftCost < rightCost) {
                if (leftCost >= cost) break;
                heap[idx] = leftNode;
                leftNode->heapIdx = idx;
                idx = left;
            } else {
                if (rightCost >= cost) break;
                heap[idx] = rightNode;
                rightNode->heapIdx = idx;
                idx = right;
            }
        }

        heap[idx] = node;
        node->heapIdx = idx;
    }

    std::vector<Node*> heap;
};

} // namespace mc
